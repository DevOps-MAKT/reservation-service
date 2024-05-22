package uns.ac.rs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uns.ac.rs.dto.request.ReservationRequestDTO;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;
import uns.ac.rs.repository.ReservationRepository;

import java.time.Instant;
import java.util.List;

@Service
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
}
