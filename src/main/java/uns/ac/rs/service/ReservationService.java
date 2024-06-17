package uns.ac.rs.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uns.ac.rs.dto.request.ReservationRequestDTO;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;
import uns.ac.rs.repository.ReservationRepository;

import java.time.Instant;
import java.util.ArrayList;
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
        long todaysMiliseconds = Instant.now().toEpochMilli();
        List<Reservation> activeReservations = reservationRepository
                .findByGuestEmailAndStatusAndEndDateAfter(guestEmail,
                        ReservationStatus.SENT,
                        ReservationStatus.ACCEPTED,
                        todaysMiliseconds);
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

    public boolean doActiveReservationsExist(String email) {
        long todaysMiliseconds = Instant.now().toEpochMilli();
        List<Reservation> hostsReservations = reservationRepository.findByHostEmailAndStatusAndEndDate(email, ReservationStatus.SENT, ReservationStatus.ACCEPTED, todaysMiliseconds);
        return hostsReservations.size() != 0;
    }

    public List<String> retrieveReservationHosts(String guestEmail) {
        List<String> hostEmails = new ArrayList<>();
        List<Reservation> reservations = reservationRepository.findByGuestEmailAndStatus(guestEmail, ReservationStatus.ACCEPTED);
        for (Reservation reservation: reservations) {
            if (!hostEmails.contains(reservation.getHostEmail())) {
                hostEmails.add(reservation.getHostEmail());
            }
        }
        return hostEmails;
    }

    public List<Long> retrieveReservationAccommodations(String guestEmail) {
        List<Long> accommodationIds = new ArrayList<>();
        List<Reservation> reservations = reservationRepository.findByGuestEmailAndStatus(guestEmail, ReservationStatus.ACCEPTED);
        for (Reservation reservation: reservations) {
            if (!accommodationIds.contains(reservation.getAccommodationId())) {
                accommodationIds.add(reservation.getAccommodationId());
            }
        }
        return accommodationIds;
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

    public List<Reservation> getPastReservations(String guestEmail) {
        long todaysMiliseconds = Instant.now().toEpochMilli();
        return reservationRepository
                .findByGuestEmailAndStatusAndEndDateBefore(guestEmail,
                        ReservationStatus.SENT,
                        ReservationStatus.ACCEPTED,
                        todaysMiliseconds);
    }
}
