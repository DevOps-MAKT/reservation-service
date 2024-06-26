package uns.ac.rs.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import uns.ac.rs.GeneralResponse;
import uns.ac.rs.model.Reservation;
import uns.ac.rs.model.ReservationStatus;

@Data
@RegisterForReflection
public class ReservationResponseDTO {

    private long id;
    private AccommodationBriefResponseDTO accommodation;
    private String hostEmail;
    private String guestEmail;
    private long startDate;
    private long endDate;
    private int noGuests;
    private ReservationStatus status;
    private int noCancellations;

    public ReservationResponseDTO() {

    }

    public ReservationResponseDTO(Reservation reservation, AccommodationBriefResponseDTO accommodation) {
        this.id = reservation.getId();
        this.accommodation = accommodation;
        this.hostEmail = reservation.getHostEmail();
        this.guestEmail = reservation.getGuestEmail();
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.noGuests = reservation.getNoGuests();
        this.status = reservation.getStatus();
    }

}
