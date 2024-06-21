package uns.ac.rs.model;

import jakarta.persistence.*;
import lombok.Data;
import uns.ac.rs.dto.request.ReservationRequestDTO;

@Entity
@Data
@Table(name="reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accommodation_id")
    private long accommodationId;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "host_email")
    private String hostEmail;

    @Column(name = "start_date")
    private long startDate;

    @Column(name = "end_date")
    private long endDate;

    @Column(name = "no_guests")
    private int noGuests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReservationStatus status;

    @Column(name = "can_be_cancelled")
    private boolean canBeCancelled;

    public Reservation() {

    }

    public Reservation(ReservationRequestDTO reservationRequestDTO, String guestEmail) {
        this.accommodationId = reservationRequestDTO.getAccommodationId();
        this.guestEmail = guestEmail;
        this.hostEmail = reservationRequestDTO.getHostEmail();
        this.startDate = reservationRequestDTO.getStartDate();
        this.endDate = reservationRequestDTO.getEndDate();
        this.noGuests = reservationRequestDTO.getNoGuests();
        this.status = ReservationStatus.SENT;
        this.canBeCancelled = true;
    }
}
