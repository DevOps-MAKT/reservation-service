package uns.ac.rs.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
@RegisterForReflection
public class AccommodationFeatureDTO {

    private String feature;

    public AccommodationFeatureDTO() {}

    public AccommodationFeatureDTO(String feature) {
        this.feature = feature;
    }

    public AccommodationFeatureDTO(LinkedHashMap data) {
        this.feature = (String) data.get("feature");
    }
}
