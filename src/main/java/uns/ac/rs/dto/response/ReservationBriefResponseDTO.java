package uns.ac.rs.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;

@Data
@RegisterForReflection
public class ReservationBriefResponseDTO {

    private long id;
    private long accommodationId;
    private String hostEmail;
    private String guestEmail;
    private long startDate;
    private long endDate;
    private int noGuests;
    private ReservationStatus status;
    private int noCancellations;

    public ReservationBriefResponseDTO() {

    }

    public ReservationBriefResponseDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.accommodationId = reservation.getAccommodationId();
        this.hostEmail = reservation.getHostEmail();
        this.guestEmail = reservation.getGuestEmail();
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.noGuests = reservation.getNoGuests();
        this.status = reservation.getStatus();
    }

    public ReservationBriefResponseDTO(Reservation reservation, int noCancellations) {
        this.id = reservation.getId();
        this.accommodationId = reservation.getAccommodationId();
        this.hostEmail = reservation.getHostEmail();
        this.guestEmail = reservation.getGuestEmail();
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.noGuests = reservation.getNoGuests();
        this.status = reservation.getStatus();
        this.noCancellations = noCancellations;
    }
}
