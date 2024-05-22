package uns.ac.rs.controller;

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
        Reservation reservation = reservationService.changeReservationStatus(reservationId, ReservationStatus.ACCEPTED);
        return Response
                .status(Response.Status.OK)
                .entity(new GeneralResponse<>(new ReservationResponseDTO(reservation), "Successfully cancelled an active reservation"))
                .build();
    }

}
