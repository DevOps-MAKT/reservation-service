package uns.ac.rs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uns.ac.rs.config.IntegrationConfig;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import uns.ac.rs.GeneralResponse;
import uns.ac.rs.MicroserviceCommunicator;
import uns.ac.rs.dto.request.ReservationRequestDTO;
import uns.ac.rs.dto.response.AccommodationBriefResponseDTO;
import uns.ac.rs.dto.response.ReservationBriefResponseDTO;
import uns.ac.rs.dto.response.ReservationResponseDTO;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;
import uns.ac.rs.service.ReservationService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Path("/reservation")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private MicroserviceCommunicator microserviceCommunicator;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private IntegrationConfig config;

    @POST
    @Path("/create")
    @RolesAllowed("guest")
    public Response createReservation(@HeaderParam("Authorization") String authorizationHeader,
                                      ReservationRequestDTO reservationRequestDTO) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/guest",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for creating reservation");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }
        logger.info("Creating reservation");
        Reservation reservation = reservationService.createReservation(reservationRequestDTO, userEmail);
        logger.info("Reservation successfully created");

        GeneralResponse automaticReservationAcceptanceStatusResponse = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/user/" + reservation.getHostEmail() + "/get-automatic-reservation-acceptance-status",
                "GET",
                authorizationHeader,
                "");


       boolean isAutomaticReservationAcceptanceStatusActive = (boolean) automaticReservationAcceptanceStatusResponse.getData();

        if (isAutomaticReservationAcceptanceStatusActive) {
            logger.info("Accepting reservation - automatic reservation status is active");
            reservation = reservationService.changeReservationStatus(reservation.getId(), ReservationStatus.ACCEPTED);
        }

        String receiverEmail = reservationRequestDTO.getHostEmail();
        String notificationType = "RESERVATION_REQUESTED";
        String senderEmail = userEmail;
        long accommodationId = reservationRequestDTO.getAccommodationId();

        String notificationBody = String.format(
                """
                {
                    "receiverEmail": "%s",
                    "notificationType": "%s",
                    "senderEmail": "%s",
                    "accommodationId": %d
                }
                """, receiverEmail, notificationType, senderEmail, accommodationId);

        GeneralResponse notification = microserviceCommunicator.processResponse(
                config.notificationServiceAPI() + "/notification/create",
                "POST",
                "",
                notificationBody);

        return Response
                .status(Response.Status.CREATED)
                .entity(new GeneralResponse<>(new ReservationBriefResponseDTO(reservation), "Reservation successfully created"))
                .build();
    }

    @GET
    @Path("/active-reservations")
    @RolesAllowed("guest")
    /*
    An active reservation is either the one that has been accepted, but it is still more than 24h before it starts OR
    the one that has been sent and is still waiting for response.
     */
    public Response getActiveReservations(@HeaderParam("Authorization") String authorizationHeader) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/guest",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for getting active reservations");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        logger.info("Retrieving active reservations for user with email {}", userEmail);
        List<Reservation> reservations = reservationService.getActiveReservations(userEmail);
        List<ReservationResponseDTO> reservationResponseDTOS = new ArrayList<>();
        for (Reservation reservation: reservations) {
            GeneralResponse accommodationResponse = microserviceCommunicator.processResponse(
                    config.accommodationServiceAPI() + "/accommodation/brief/" + reservation.getAccommodationId(),
                    "GET",
                    authorizationHeader,
                    "");
            AccommodationBriefResponseDTO accommodation = new AccommodationBriefResponseDTO((LinkedHashMap) accommodationResponse.getData());
            reservationResponseDTOS.add(new ReservationResponseDTO(reservation, accommodation));
        }
        logger.info("Successfully retrieved active reservations for user with email {}", userEmail);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(reservationResponseDTOS, "Successfully retrieved active reservations"))
                .build();
    }

    @PATCH
    @Path("/deactivate/{reservation_id}")
    @RolesAllowed("guest")
    public Response deactivateReservation(@HeaderParam("Authorization") String authorizationHeader,
                                          @PathParam("reservation_id") long reservationId) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/guest",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for deactivating reservation");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        logger.info("Setting reservations status to CANCELLED");
        Reservation reservation = reservationService.changeReservationStatus(reservationId, ReservationStatus.CANCELLED);
        GeneralResponse noCancellations = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/user/append-cancellation",
                "GET",
                authorizationHeader,
                "");
        logger.info("Successfully deactivated reservation");

        String receiverEmail = reservation.getHostEmail();
        String notificationType = "RESERVATION_CANCELLED";
        String senderEmail = userEmail;
        long accommodationId = reservation.getAccommodationId();

        String notificationBody = String.format(
                """
                {
                    "receiverEmail": "%s",
                    "notificationType": "%s",
                    "senderEmail": "%s",
                    "accommodationId": %d
                }
                """, receiverEmail, notificationType, senderEmail, accommodationId);
        GeneralResponse notification = microserviceCommunicator.processResponse(
                config.notificationServiceAPI() + "/notification/create",
                "POST",
                "",
                notificationBody);

        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationBriefResponseDTO(reservation), "Successfully cancelled an active reservation"))
                .build();
    }

    @GET
    @Path("/requested-reservations")
    @RolesAllowed("host")
    public Response getAllRequestedReservations(@HeaderParam("Authorization") String authorizationHeader) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/host",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for getting all requested reservations");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        logger.info("Retrieving requested reservations");
        List<Reservation> requestedReservations = reservationService.getRequestedReservations(userEmail);
        logger.info("Successfully retrieved requested reservations");
        List<ReservationBriefResponseDTO> reservationResponseDTOS = new ArrayList<>();
        logger.info("Retrieving number of cancellations for each guest");
        for (Reservation requestedReservation: requestedReservations) {
            GeneralResponse noCancellations = microserviceCommunicator.processResponse(
                    config.userServiceAPI() + "/user/no-cancellations/" + requestedReservation.getGuestEmail(),
                    "GET",
                    authorizationHeader,
                    "");
            reservationResponseDTOS.add(new ReservationBriefResponseDTO(requestedReservation, (int) noCancellations.getData()));
        }
        logger.info("Retrieved number of cancellations for each guest");
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(reservationResponseDTOS, "Successfully retrieved requested reservations"))
                .build();
    }

    @PATCH
    @Path("/{reservation_id}/reject")
    @RolesAllowed("host")
    public Response reject(@HeaderParam("Authorization") String authorizationHeader,
                               @PathParam("reservation_id") long reservationId) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/host",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for rejecting a reservation");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        logger.info("Rejecting a reservation with id {}", reservationId);
        Reservation reservation = reservationService.rejectReservation(reservationId);
        logger.info("Successfully rejected a reservation with id {}", reservationId);

        String receiverEmail = userEmail;
        String notificationType = "RESERVATION_REQUEST_ANSWERED";
        String senderEmail = reservation.getHostEmail();

        String notificationBody = String.format(
                """
                {
                    "receiverEmail": "%s",
                    "notificationType": "%s",
                    "senderEmail": "%s",
                    "requestAccepted": %b
                }
                """, receiverEmail, notificationType, senderEmail, false);

        GeneralResponse notification = microserviceCommunicator.processResponse(
                config.notificationServiceAPI() + "/notification/create",
                "POST",
                "",
                notificationBody);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationBriefResponseDTO(reservation), "Successfully rejected reservation"))
                .build();
    }

    @PATCH
    @Path("/{reservation_id}/accept")
    @RolesAllowed("host")
    public Response accept(@HeaderParam("Authorization") String authorizationHeader,
                           @PathParam("reservation_id") long reservationId) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/host",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for accepting a reservation");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        logger.info("Accepting a reservation with id {}", reservationId);
        Reservation reservation = reservationService.acceptReservation(reservationId);
        logger.info("Successfully accepted a reservation with id {}", reservationId);


        String receiverEmail = userEmail;
        String notificationType = "RESERVATION_REQUEST_ANSWERED";
        String senderEmail = reservation.getHostEmail();

        String notificationBody = String.format(
                """
                {
                    "receiverEmail": "%s",
                    "notificationType": "%s",
                    "senderEmail": "%s",
                    "requestAccepted": %b
                }
                """, receiverEmail, notificationType, senderEmail, true);

        GeneralResponse notification = microserviceCommunicator.processResponse(
                config.notificationServiceAPI() + "/notification/create",
                "POST",
                "",
                notificationBody);

        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationBriefResponseDTO(reservation), "Successfully accepted reservation"))
                .build();
    }

    @GET
    @Path("/{accommodation_id}")
    @PermitAll
    public Response getReservationsForAccommodation(@PathParam("accommodation_id") long accommodationId) {
        logger.info("Retrieving reservations for accommodation with id {}", accommodationId);
        List<Reservation> reservations = reservationService.findReservationsBasedOnAccommodation(accommodationId);
        logger.info("Successfully retrieved reservations for accommodation with id {}", accommodationId);
        List<ReservationBriefResponseDTO> reservationResponseDTOS = new ArrayList<>();
        for (Reservation reservation: reservations) {
            reservationResponseDTOS.add(new ReservationBriefResponseDTO(reservation));
        }

        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(reservationResponseDTOS, "Successfully retrieved reservation for accommodation"))
                .build();

    }

    @GET
    @Path("/are-reservations-active/{email}")
    @RolesAllowed("guest")
    public Response areReservationsActive(@HeaderParam("Authorization") String authorizationHeader,
                                          @PathParam("email") String email) {
        boolean areReservationsActive = true;
        logger.info("Retrieving active reservations");
        List<Reservation> reservations = reservationService.getActiveReservations(email);
        logger.info("Successfully retrieved active reservations");
        if (reservations.size() == 0) {
            areReservationsActive = false;
        }
        return Response
                .ok()
                .entity(new GeneralResponse<>(areReservationsActive, "Successfully retrieved if reservations are active"))
                .build();
    }

    @GET
    @Path("/do-active-reservations-exist/{email}")
    @RolesAllowed("host")
    public Response doActiveReservationsExist(@HeaderParam("Authorization") String authorizationHeader,
                                          @PathParam("email") String email) {

        boolean doActiveReservationsExist  = reservationService.doActiveReservationsExist(email);
        if (doActiveReservationsExist) {
            return Response
                    .ok()
                    .entity(new GeneralResponse<>(true, "There are currently active reservations"))
                    .build();
        }
        return Response
                .ok()
                .entity(new GeneralResponse<>(false, "There are currently no active reservations"))
                .build();
    }

    @GET
    @Path("/retrieve-reservation-hosts/{guest_email}")
    @RolesAllowed("guest")
    public Response retrieveReservationHosts(@PathParam("guest_email") String guestEmail) {
        List<String> hostEmails = reservationService.retrieveReservationHosts(guestEmail);
        return Response
                .ok()
                .entity(new GeneralResponse<>(hostEmails, "Successfully retrieved unique hosts"))
                .build();
    }

    @GET
    @Path("/retrieve-reservation-accommodations/{guest_email}")
    @RolesAllowed("guest")
    public Response retrieveReservationAccommodations(@PathParam("guest_email") String guestEmail) {
        List<Long> accommodationIds = reservationService.retrieveReservationAccommodations(guestEmail);
        return Response
                .ok()
                .entity(new GeneralResponse<>(accommodationIds, "Successfully retrieved unique accommodations"))
                .build();
    }

    @GET
    @Path("/past-reservations")
    @RolesAllowed("guest")
    public Response getPastReservations(@HeaderParam("Authorization") String authorizationHeader) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                config.userServiceAPI() + "/auth/authorize/guest",
                "GET",
                authorizationHeader,
                "");
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            logger.warn("Unauthorized access for getting active reservations");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        logger.info("Retrieving past reservations for user with email {}", userEmail);
        List<Reservation> reservations = reservationService.getPastReservations(userEmail);
        List<ReservationResponseDTO> reservationResponseDTOS = new ArrayList<>();
        for (Reservation reservation: reservations) {
            GeneralResponse accommodationResponse = microserviceCommunicator.processResponse(
                    config.accommodationServiceAPI() + "/accommodation/brief/" + reservation.getAccommodationId(),
                    "GET",
                    authorizationHeader,
                    "");
            AccommodationBriefResponseDTO accommodation = new AccommodationBriefResponseDTO((LinkedHashMap) accommodationResponse.getData());
            reservationResponseDTOS.add(new ReservationResponseDTO(reservation, accommodation));
        }
        logger.info("Successfully retrieved past reservations for user with email {}", userEmail);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(reservationResponseDTOS, "Successfully retrieved past reservations"))
                .build();
    }

}
