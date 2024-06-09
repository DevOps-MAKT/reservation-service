package uns.ac.rs.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@RegisterForReflection
public class LocationDTO {
    private String country;
    private String city;

    public LocationDTO(String country, String city) {
        this.country = country;
        this.city = city;
    }

    public LocationDTO() {

    }

    public LocationDTO(LinkedHashMap data) {
        this.country = (String) data.get("country");
        this.city = (String) data.get("city");
    }
}
