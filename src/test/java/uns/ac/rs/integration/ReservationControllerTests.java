package uns.ac.rs.integration;

import uns.ac.rs.config.IntegrationConfig;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("requested-reservations")
    URL getRequestedReservationsEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("3/accept")
    URL acceptRequestEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("2/reject")
    URL rejectRequestEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("are-reservations-active/guest@gmail.com")
    URL areReservationsActiveEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("do-active-reservations-exist/host@gmail.com")
    URL doActiveReservationsExistEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("retrieve-reservation-hosts/guest@gmail.com")
    URL retrieveReservationHostsEndpoint;

    @TestHTTPEndpoint(ReservationController.class)
    @TestHTTPResource("retrieve-reservation-accommodations/guest@gmail.com")
    URL retrieveReservationAccommodationsEndpoint;

    @InjectMock
    private MicroserviceCommunicator microserviceCommunicator;

    @Autowired
    private IntegrationConfig config;
    @Test
    @Order(1)
    public void whenCreateReservationWithAutomaticReservationOn_thenReturnCreatedReservationWithAcceptedStatus() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(true, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/get-automatic-reservation-acceptance-status",
                        "GET",
                        "Bearer good-jwt",
                        "");

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
    public void whenCreateReservationWithAutomaticAcceptanceOff_thenReturnCreatedReservationWithSentStatus() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(false, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/get-automatic-reservation-acceptance-status",
                        "GET",
                        "Bearer good-jwt",
                        "");

        ReservationRequestDTO reservationRequestDTO = new ReservationRequestDTO();
        reservationRequestDTO.setAccommodationId(1L);
        reservationRequestDTO.setStartDate(1739142000000L); // 10.2.2025.
        reservationRequestDTO.setEndDate(1739746800000L); // 17.2.2025.
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
                .body("data.id", equalTo(2))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1739142000000L))
                .body("data.endDate", equalTo(1739746800000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("SENT"))
                .body("message", equalTo("Reservation successfully created"));
    }

    @Test
    @Order(3)
    public void whenCreateAnotherReservationWithAutomaticAcceptanceOff_thenReturnCreatedReservationWithSentStatus() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(false, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/get-automatic-reservation-acceptance-status",
                        "GET",
                        "Bearer good-jwt",
                        "");

        ReservationRequestDTO reservationRequestDTO = new ReservationRequestDTO();
        reservationRequestDTO.setAccommodationId(1L);
        reservationRequestDTO.setStartDate(1740870000000L); // 2.3.2025.
        reservationRequestDTO.setEndDate(1741993200000L); // 15.3.2025.
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
                .body("data.id", equalTo(3))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1740870000000L))
                .body("data.endDate", equalTo(1741993200000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("SENT"))
                .body("message", equalTo("Reservation successfully created"));
    }

    @Test
    @Order(4)
    public void whenCreateAnotherReservationWithAutomaticAcceptanceOffWithOverlappingDates_thenReturnCreatedReservationWithSentStatus() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(false, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/get-automatic-reservation-acceptance-status",
                        "GET",
                        "Bearer good-jwt",
                        "");

        ReservationRequestDTO reservationRequestDTO = new ReservationRequestDTO();
        reservationRequestDTO.setAccommodationId(1L);
        reservationRequestDTO.setStartDate(1741042800000L); // 4.3.2025.
        reservationRequestDTO.setEndDate(1741215600000L); // 6.3.2025.
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
                .body("data.id", equalTo(4))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1741042800000L))
                .body("data.endDate", equalTo(1741215600000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("SENT"))
                .body("message", equalTo("Reservation successfully created"));
    }

    @Test
    @Order(5)
    public void whenGetActiveReservations_thenReturnSentOrAcceptedWithMoreThan24HoursBeforeItStarts() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt",
                        "");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(getActiveReservationsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(4))
                .body("message", equalTo("Successfully retrieved active reservations"));
    }

    @Test
    @Order(6)
    public void whenDeactivateReservation_thenChangeReservationStatusToCancelled() {
        doReturn(new GeneralResponse("guest@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/guest",
                        "GET",
                        "Bearer good-jwt",
                        "");

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

    @Test
    @Order(7)
    public void whenGetAllRequestedReservations_thenReturnSentReservationsWithAmountOfCancellations() {
        doReturn(new GeneralResponse("host@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/host",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(5, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/no-cancellations/guest@gmail.com",
                        "GET",
                        "Bearer good-jwt",
                        "");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(getRequestedReservationsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(3))
                .body("message", equalTo("Successfully retrieved requested reservations"));
    }

    @Test
    @Order(8)
    public void whenAcceptRequest_thenRejectRequestsInTheDateRangeAndReturnAcceptedRequest() {
        doReturn(new GeneralResponse("host@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/host",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(5, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/no-cancellations/guest@gmail.com",
                        "GET",
                        "Bearer good-jwt",
                        "");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .patch(acceptRequestEndpoint)
        .then()
                .statusCode(200)
                .body("data.id", equalTo(3))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1740870000000L))
                .body("data.endDate", equalTo(1741993200000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("ACCEPTED"))
                .body("message", equalTo("Successfully accepted reservation"));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(getRequestedReservationsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(1))
                .body("message", equalTo("Successfully retrieved requested reservations"));
    }

    @Test
    @Order(9)
    public void whenRejectReservation_thenReturnRejectedReservation() {
        doReturn(new GeneralResponse("host@gmail.com", "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/auth/authorize/host",
                        "GET",
                        "Bearer good-jwt",
                        "");

        doReturn(new GeneralResponse(5, "200"))
                .when(microserviceCommunicator)
                .processResponse(config.userServiceAPI() + "/user/no-cancellations/guest@gmail.com",
                        "GET",
                        "Bearer good-jwt",
                        "");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .patch(rejectRequestEndpoint)
        .then()
                .statusCode(200)
                .body("data.id", equalTo(2))
                .body("data.accommodationId", equalTo(1))
                .body("data.hostEmail", equalTo("host@gmail.com"))
                .body("data.guestEmail", equalTo("guest@gmail.com"))
                .body("data.startDate", equalTo(1739142000000L))
                .body("data.endDate", equalTo(1739746800000L))
                .body("data.noGuests", equalTo(5))
                .body("data.status", equalTo("REJECTED"))
                .body("message", equalTo("Successfully rejected reservation"));

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(getRequestedReservationsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(0))
                .body("message", equalTo("Successfully retrieved requested reservations"));
    }

    @Test
    @Order(10)
    public void whenThereAreActiveReservations_thenReturnTrue() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(areReservationsActiveEndpoint)
        .then()
                .statusCode(200)
                .body("data", equalTo(true))
                .body("message", equalTo("Successfully retrieved if reservations are active"));
    }

    @Test
    @Order(11)
    public void whenThereAreActiveHostReservations_thenReturnTrue() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(doActiveReservationsExistEndpoint)
        .then()
                .statusCode(200)
                .body("data", equalTo(true))
                .body("message", equalTo("There are currently active reservations"));
    }

    @Test
    @Order(12)
    public void whenRetrieveReservationForAHost_thenReturnFoundReservations() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(retrieveReservationHostsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(1))
                .body("message", equalTo("Successfully retrieved unique hosts"));
    }

    @Test
    @Order(13)
    public void whenRetrieveReservationForAnAccommodation_thenReturnFoundReservations() {
        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer good-jwt")
        .when()
                .get(retrieveReservationAccommodationsEndpoint)
        .then()
                .statusCode(200)
                .body("data.size()", equalTo(1))
                .body("message", equalTo("Successfully retrieved unique accommodations"));
    }
}
