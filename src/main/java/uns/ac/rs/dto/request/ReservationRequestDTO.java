package uns.ac.rs.dto.request;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ReservationRequestDTO {

    private long accommodationId;

    private String hostEmail;

    private long startDate;

    private long endDate;

    private int noGuests;

    public ReservationRequestDTO() {

    }
}
