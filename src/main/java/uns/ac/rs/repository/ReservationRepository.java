package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.springframework.stereotype.Repository;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;

import java.util.List;

@Repository
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> findByGuestEmailAndStatusAndEndDateAfter(String guestEmail, ReservationStatus status1, ReservationStatus status2, long endDate) {
        return list("guestEmail = ?1 and (status = ?2 or status = ?3) and endDate > ?4", guestEmail, status1, status2, endDate);
    }

    public List<Reservation> findByGuestEmailAndStatusAndEndDateBefore(String guestEmail, ReservationStatus status1, ReservationStatus status2, long endDate) {
        return list("guestEmail = ?1 and (status = ?2 or status = ?3) and endDate < ?4", guestEmail, status1, status2, endDate);
    }

    public List<Reservation> findByHostEmailAndStatus(String hostEmail, ReservationStatus status) {
        return list("hostEmail = ?1 and status = ?2", hostEmail, status);
    }

    public List<Reservation> findByAccommodationIdAndStatus(long accommodationId, ReservationStatus status) {
        return list("accommodationId = ?1 and status = ?2", accommodationId, status);
    }

    public List<Reservation> findByHostEmailAndStatusAndEndDate(String hostEmail, ReservationStatus status1, ReservationStatus status2, long endDate) {
        return list("hostEmail = ?1 and (status = ?2 or status = ?3) and endDate > ?4", hostEmail, status1, status2, endDate);
    }

    public List<Reservation> findByGuestEmailAndStatus(String guestEmail, ReservationStatus status) {
        return list("guestEmail = ?1 and status = ?2", guestEmail, status);
    }
}
