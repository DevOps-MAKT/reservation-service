package uns.ac.rs.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uns.ac.rs.dto.request.ReservationRequestDTO;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;
import uns.ac.rs.repository.ReservationRepository;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    public Reservation createReservation(ReservationRequestDTO reservationRequestDTO, String guestEmail) {
        Reservation reservation = new Reservation(reservationRequestDTO, guestEmail);
        reservationRepository.persist(reservation);
        return reservation;
    }

    public Reservation changeReservationStatus(long reservationId, ReservationStatus reservationStatus){
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.setStatus(reservationStatus);
        reservationRepository.persist(reservation);
        return reservation;
    }

    public List<Reservation> getActiveReservations(String guestEmail) {
        List<Reservation> activeReservations = reservationRepository
                .findByGuestEmailAndStatus(guestEmail, ReservationStatus.SENT, ReservationStatus.ACCEPTED);
        long todaysMiliseconds = Instant.now().toEpochMilli();
        long dayMiliseconds = 24*60*60*1000;
        for (Reservation activeReservation: activeReservations) {
            if (activeReservation.isCanBeCancelled() && Math.abs(todaysMiliseconds - activeReservation.getStartDate()) <= dayMiliseconds) {
                activeReservation.setCanBeCancelled(false);
                reservationRepository.persist(activeReservation);
            }
        }
        return activeReservations;
    }

    public List<Reservation> getRequestedReservations(String hostEmail) {
        return reservationRepository.findByHostEmailAndStatus(hostEmail, ReservationStatus.SENT);
    }

    public Reservation rejectReservation(long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.setStatus(ReservationStatus.REJECTED);
        reservationRepository.persist(reservation);
        return reservation;
    }

    public Reservation acceptReservation(long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId);
        reservation.setStatus(ReservationStatus.ACCEPTED);
        reservationRepository.persist(reservation);
        rejectSentReservationsInSamePeriod(reservation);
        return reservation;
    }

    public List<Reservation> findReservationsBasedOnAccommodation(long accommodationId) {
        return reservationRepository.findByAccommodationIdAndStatus(accommodationId,
                ReservationStatus.ACCEPTED);
    }

    private void rejectSentReservationsInSamePeriod(Reservation reservation) {
        List<Reservation> reservations = reservationRepository.findByAccommodationIdAndStatus(reservation.getAccommodationId(), ReservationStatus.SENT);
        for (Reservation sentReservation: reservations) {
            if (isDateInRange(sentReservation.getStartDate(),
                    reservation.getStartDate(),
                    reservation.getEndDate())
            || isDateInRange(sentReservation.getEndDate(),
                    reservation.getStartDate(),
                    reservation.getEndDate())
            || areDatesContainingOriginalDates(sentReservation.getStartDate(),
                    sentReservation.getEndDate(),
                    reservation.getStartDate(),
                    reservation.getEndDate())) {
                sentReservation.setStatus(ReservationStatus.REJECTED);
                reservationRepository.persist(sentReservation);
            }
        }
    }

    private boolean isDateInRange(long date, long startDate, long endDate) {
        return date >= startDate && date <= endDate;
    }

    private boolean areDatesContainingOriginalDates(long startDate, long endDate, long originalStartDate, long originalEndDate) {
        return startDate <= originalStartDate && endDate >= originalEndDate;
    }
}
