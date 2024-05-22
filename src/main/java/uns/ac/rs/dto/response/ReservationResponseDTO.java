package uns.ac.rs.dto.response;

import lombok.Data;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;

@Data
public class ReservationResponseDTO {

    private long id;

    private long accommodationId;

    private String hostEmail;

    private String guestEmail;

    private long startDate;

    private long endDate;

    private int noGuests;

    private ReservationStatus status;

    public ReservationResponseDTO() {

    }

    public ReservationResponseDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.accommodationId = reservation.getAccommodationId();
        this.hostEmail = reservation.getHostEmail();
        this.guestEmail = reservation.getGuestEmail();
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.noGuests = reservation.getNoGuests();
        this.status = reservation.getStatus();
    }
}
