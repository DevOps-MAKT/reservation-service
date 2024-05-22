package uns.ac.rs.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.springframework.stereotype.Repository;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;

import java.util.List;

@Repository
public class ReservationRepository implements PanacheRepository<Reservation> {

    public List<Reservation> findByGuestEmailAndStatus(String guestEmail, ReservationStatus status1, ReservationStatus status2) {
        return list("guestEmail = ?1 and (status = ?2 or status = ?3)", guestEmail, status1, status2);
    }
}
