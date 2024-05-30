package uns.ac.rs.controller;

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
import uns.ac.rs.dto.response.ReservationResponseDTO;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;
import uns.ac.rs.service.ReservationService;

import java.util.ArrayList;
import java.util.List;

@Path("/reservation")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationController {

    @Autowired
    private MicroserviceCommunicator microserviceCommunicator;

    @Autowired
    private ReservationService reservationService;

    @POST
    @Path("/create")
    @RolesAllowed("guest")
    public Response createReservation(@HeaderParam("Authorization") String authorizationHeader,
                                      ReservationRequestDTO reservationRequestDTO) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                "http://localhost:8001/user-service/auth/authorize/guest",
                "GET",
                authorizationHeader);
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }
        Reservation reservation = reservationService.createReservation(reservationRequestDTO, userEmail);

        GeneralResponse automaticReservationAcceptanceStatusResponse = microserviceCommunicator.processResponse(
                "http://localhost:8001/user-service/user/get-automatic-reservation-acceptance-status",
                "GET",
                authorizationHeader
        );

        boolean isAutomaticReservationAcceptanceStatusActive = (boolean) automaticReservationAcceptanceStatusResponse.getData();

        if (isAutomaticReservationAcceptanceStatusActive) {
            reservation = reservationService.changeReservationStatus(reservation.getId(), ReservationStatus.ACCEPTED);
        }

        return Response
                .status(Response.Status.CREATED)
                .entity(new GeneralResponse<>(new ReservationResponseDTO(reservation), "Reservation successfully created"))
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
                "http://localhost:8001/user-service/auth/authorize/guest",
                "GET",
                authorizationHeader);
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }
        List<Reservation> reservations = reservationService.getActiveReservations(userEmail);
        List<ReservationResponseDTO> reservationResponseDTOS = new ArrayList<>();
        for (Reservation reservation: reservations) {
            reservationResponseDTOS.add(new ReservationResponseDTO(reservation));
        }
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
                "http://localhost:8001/user-service/auth/authorize/guest",
                "GET",
                authorizationHeader);
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }
        Reservation reservation = reservationService.changeReservationStatus(reservationId, ReservationStatus.CANCELLED);
        GeneralResponse noCancellations = microserviceCommunicator.processResponse(
                "http://localhost:8001/user-service/user/append-cancellation",
                "GET",
                authorizationHeader);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationResponseDTO(reservation), "Successfully cancelled an active reservation"))
                .build();
    }

    @GET
    @Path("/requested-reservations")
    @RolesAllowed("host")
    public Response getAllRequestedReservations(@HeaderParam("Authorization") String authorizationHeader) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                "http://localhost:8001/user-service/auth/authorize/host",
                "GET",
                authorizationHeader);
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }
        List<Reservation> requestedReservations = reservationService.getRequestedReservations(userEmail);
        List<ReservationResponseDTO> reservationResponseDTOS = new ArrayList<>();
        for (Reservation requestedReservation: requestedReservations) {
            GeneralResponse noCancellations = microserviceCommunicator.processResponse(
                    "http://localhost:8001/user-service/user/no-cancellations/" + requestedReservation.getGuestEmail(),
                    "GET",
                    authorizationHeader);
            reservationResponseDTOS.add(new ReservationResponseDTO(requestedReservation, (int) noCancellations.getData()));
        }
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
                "http://localhost:8001/user-service/auth/authorize/host",
                "GET",
                authorizationHeader);
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        Reservation reservation = reservationService.rejectReservation(reservationId);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationResponseDTO(reservation), "Successfully rejected reservation"))
                .build();
    }

    @PATCH
    @Path("/{reservation_id}/accept")
    @RolesAllowed("host")
    public Response accept(@HeaderParam("Authorization") String authorizationHeader,
                           @PathParam("reservation_id") long reservationId) {
        GeneralResponse response = microserviceCommunicator.processResponse(
                "http://localhost:8001/user-service/auth/authorize/host",
                "GET",
                authorizationHeader);
        String userEmail = (String) response.getData();
        if (userEmail.equals("")) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        Reservation reservation = reservationService.acceptReservation(reservationId);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationResponseDTO(reservation), "Successfully accepted reservation"))
                .build();
    }

    @GET
    @Path("/{accommodation_id}")
    @PermitAll
    public Response getReservationsForAccommodation(@PathParam("accommodation_id") long accommodationId) {
        List<Reservation> reservations = reservationService.findReservationsBasedOnAccommodation(accommodationId);
        List<ReservationResponseDTO> reservationResponseDTOS = new ArrayList<>();
        for (Reservation reservation: reservations) {
            reservationResponseDTOS.add(new ReservationResponseDTO(reservation));
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
        List<Reservation> reservations = reservationService.getActiveReservations(email);
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

}
