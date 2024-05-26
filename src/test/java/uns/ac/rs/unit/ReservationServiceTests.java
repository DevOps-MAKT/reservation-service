package uns.ac.rs.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uns.ac.rs.dto.request.ReservationRequestDTO;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;
import uns.ac.rs.repository.ReservationRepository;
import uns.ac.rs.service.ReservationService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTests {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    public void testCreateReservation() {
        ReservationRequestDTO reservationRequestDTO = new ReservationRequestDTO();
        reservationRequestDTO.setAccommodationId(1L);
        reservationRequestDTO.setStartDate(1735686000000L); // 1.1.2025.
        reservationRequestDTO.setEndDate(1736290800000L); // 8.1.2025.
        reservationRequestDTO.setHostEmail("host@gmail.com");
        reservationRequestDTO.setNoGuests(5);

        Reservation reservation = reservationService.createReservation(reservationRequestDTO, "guest@gmail.com");

        verify(reservationRepository, times(1)).persist(reservation);

        assertEquals(reservation.getAccommodationId(), 1);
        assertEquals(reservation.getStartDate(), 1735686000000L);
        assertEquals(reservation.getEndDate(), 1736290800000L);
        assertEquals(reservation.getHostEmail(), "host@gmail.com");
        assertEquals(reservation.getNoGuests(), 5);
        assertEquals(reservation.getGuestEmail(), "guest@gmail.com");
    }

    @Test
    public void testChangeReservationStatus() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setAccommodationId(1L);
        reservation.setStartDate(1735686000000L); // 1.1.2025.
        reservation.setEndDate(1736290800000L); // 8.1.2025.
        reservation.setHostEmail("host@gmail.com");
        reservation.setNoGuests(5);
        reservation.setStatus(ReservationStatus.ACCEPTED);

        when(reservationRepository.findById(1L)).thenReturn(reservation);

        reservationService.changeReservationStatus(1L, ReservationStatus.CANCELLED);

        verify(reservationRepository, times(1)).persist(reservation);

        assertEquals(reservation.getStatus(), ReservationStatus.CANCELLED);
    }

    @Test
    public void testFindActiveReservations() {
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setAccommodationId(1L);
        reservation1.setStartDate(1735686000000L); // 1.1.2025.
        reservation1.setEndDate(1736290800000L); // 8.1.2025.
        reservation1.setHostEmail("host@gmail.com");
        reservation1.setNoGuests(5);
        reservation1.setStatus(ReservationStatus.ACCEPTED);

        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setAccommodationId(1L);
        reservation2.setStartDate(1735686000000L); // 1.1.2025.
        reservation2.setEndDate(1736290800000L); // 8.1.2025.
        reservation2.setHostEmail("host@gmail.com");
        reservation2.setNoGuests(5);
        reservation2.setStatus(ReservationStatus.REJECTED);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation1);

        when(reservationRepository.findByGuestEmailAndStatusAndEndDate(
                eq("guest@gmail.com"),
                eq(ReservationStatus.SENT),
                eq(ReservationStatus.ACCEPTED),
                anyLong()))
                .thenReturn(reservations);

        List<Reservation> activeReservations = reservationService.getActiveReservations("guest@gmail.com");


        assertEquals(activeReservations.size(), 1);
    }

    @Test
    public void rejectReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setAccommodationId(1L);
        reservation.setStartDate(1735686000000L); // 1.1.2025.
        reservation.setEndDate(1736290800000L); // 8.1.2025.
        reservation.setHostEmail("host@gmail.com");
        reservation.setNoGuests(5);
        reservation.setStatus(ReservationStatus.SENT);

        when(reservationRepository.findById(1L)).thenReturn(reservation);

        Reservation updatedReservation = reservationService.rejectReservation(1L);

        assertEquals(updatedReservation.getStatus(), ReservationStatus.REJECTED);
    }

    @Test
    public void acceptReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setAccommodationId(1L);
        reservation.setStartDate(1735686000000L); // 1.1.2025.
        reservation.setEndDate(1736290800000L); // 8.1.2025.
        reservation.setHostEmail("host@gmail.com");
        reservation.setNoGuests(5);
        reservation.setStatus(ReservationStatus.SENT);

        when(reservationRepository.findById(1L)).thenReturn(reservation);

        Reservation updatedReservation = reservationService.acceptReservation(1L);

        assertEquals(updatedReservation.getStatus(), ReservationStatus.ACCEPTED);
    }

    @Test
    public void testDoActiveReservationsExist() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setAccommodationId(1L);
        reservation.setStartDate(1735686000000L); // 1.1.2025.
        reservation.setEndDate(1736290800000L); // 8.1.2025.
        reservation.setHostEmail("host@gmail.com");
        reservation.setNoGuests(5);
        reservation.setStatus(ReservationStatus.SENT);

        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        when(reservationRepository.findByHostEmailAndStatusAndEndDate(
                eq("host@gmail.com"),
                eq(ReservationStatus.SENT),
                eq(ReservationStatus.ACCEPTED),
                anyLong()))
                .thenReturn(reservations);

        boolean doActiveReservationsExist = reservationService.doActiveReservationsExist("host@gmail.com");

        assertTrue(doActiveReservationsExist);
    }

    @Test
    public void testRetrieveReservationAccommodations() {
        String guestEmail = "guest@example.com";
        Reservation reservation1 = new Reservation();
        reservation1.setId(1L);
        reservation1.setAccommodationId(1L);
        Reservation reservation2 = new Reservation();
        reservation2.setId(2L);
        reservation2.setAccommodationId(2L);
        Reservation reservation3 = new Reservation();
        reservation3.setId(3L);
        reservation3.setAccommodationId(1L); // Repeated accommodationId
        when(reservationRepository.findByGuestEmailAndStatus(guestEmail, ReservationStatus.ACCEPTED))
                .thenReturn(List.of(reservation1, reservation2, reservation3));

        List<Long> accommodationIds = reservationService.retrieveReservationAccommodations(guestEmail);

        assertNotNull(accommodationIds);
        assertEquals(2, accommodationIds.size());
        assertTrue(accommodationIds.contains(1L));
        assertTrue(accommodationIds.contains(2L));
    }

}
