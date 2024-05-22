package uns.ac.rs.integration;

import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import uns.ac.rs.GeneralResponse;
import uns.ac.rs.MicroserviceCommunicator;
import uns.ac.rs.controller.ReservationController;
import uns.ac.rs.dto.request.ReservationRequestDTO;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doReturn;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationControllerTests {

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("create")
    URL createReservationEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("active-reservations")
    URL getActiveReservationsEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("deactivate/1")
    URL deactivateReservationEndpoint;

    @InjectMock
    private MicroserviceCommunicator microserviceCommunicator;

    @Test
    @Order(1)
    public void whenCreateReservation_thenReturnCreatedReservation() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse("http://localhost:8001/user-service/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt");

        doReturn(new GeneralResponse(true, "200"))
                .when(microserviceCommunicator)
                .processResponse("http://localhost:8001/user-service/user/get-automatic-reservation-acceptance-status",
                        "GET",
                        "Bearer good-jwt");

        ReservationRequestDTO reservationRequestDTO = new ReservationRequestDTO();
        reservationRequestDTO.setAccommodationId(1L);
        reservationRequestDTO.setStartDate(1735686000000L); // 1.1.2025.
        reservationRequestDTO.setEndDate(1736290800000L); // 8.1.2025.
        reservationRequestDTO.setHostEmail("host@gmail.com");
        reservationRequestDTO.setNoGuests(5);


        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
                .body(reservationRequestDTO)
        .when()
                .post(createReservationEndpoint)
        .then()
                .statusCode(201)
                .body("data.id", equalTo(1))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1735686000000L))
                .body("data.endDate", equalTo(1736290800000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("ACCEPTED"))
                .body("message", equalTo("Reservation successfully created"));
    }

    @Test
    @Order(2)
    public void whenGetActiveReservations_thenReturnSentOrAcceptedWithMoreThan24HoursBeforeItStarts() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse("http://localhost:8001/user-service/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(getActiveReservationsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(1))
                .body("message", equalTo("Successfully retrieved active reservations"));
    }

    @Test
    @Order(3)
    public void whenDeactivateReservation_thenChangeReservationStatusToCancelled() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse("http://localhost:8001/user-service/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .patch(deactivateReservationEndpoint)
        .then()
                .statusCode(200)
                .body("data.id", equalTo(1))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1735686000000L))
                .body("data.endDate", equalTo(1736290800000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("CANCELLED"))
                .body("message", equalTo("Successfully cancelled an active reservation"));
    }
}
