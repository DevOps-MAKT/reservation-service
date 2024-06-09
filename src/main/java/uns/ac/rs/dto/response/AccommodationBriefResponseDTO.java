package uns.ac.rs.dto.response;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import uns.ac.rs.dto.AccommodationFeatureDTO;
import uns.ac.rs.dto.LocationDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@RegisterForReflection
public class AccommodationBriefResponseDTO {
    private Long id;
    private String name;
    private LocationDTO location;
    private List<AccommodationFeatureDTO> accommodationFeatures;
    private String photographURL;
    private int minimumNoGuests;
    private int maximumNoGuests;
    private String hostEmail;
    private double price;
    private boolean pricePerGuest;
    private double avgRating;

    public AccommodationBriefResponseDTO() {
    }

    public AccommodationBriefResponseDTO(Long id, String name, LocationDTO location, List<AccommodationFeatureDTO> accommodationFeatures, String photographURL, int minimumNoGuests, int maximumNoGuests, String hostEmail, double price, boolean pricePerGuest, double avgRating) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.accommodationFeatures = accommodationFeatures;
        this.photographURL = photographURL;
        this.minimumNoGuests = minimumNoGuests;
        this.maximumNoGuests = maximumNoGuests;
        this.hostEmail = hostEmail;
        this.price = price;
        this.pricePerGuest = pricePerGuest;
        this.avgRating = avgRating;
    }

    public AccommodationBriefResponseDTO(LinkedHashMap data) {
        this.id = ((Number) data.get("id")).longValue();
        this.name = (String) data.get("name");
        this.location = new LocationDTO((LinkedHashMap)data.get("location"));
        this.accommodationFeatures = new ArrayList<>();
        for (LinkedHashMap featureData : (ArrayList<LinkedHashMap>) data.get("accommodationFeatures")) {
            accommodationFeatures.add(new AccommodationFeatureDTO(featureData));
        }
        this.photographURL = (String) data.get("photographURL");
        this.minimumNoGuests = (int) data.get("minimumNoGuests");
        this.maximumNoGuests = (int) data.get("maximumNoGuests");
        this.hostEmail = (String) data.get("hostEmail");
        this.price = (double) data.get("price");
        this.pricePerGuest = (boolean) data.get("pricePerGuest");
        this.avgRating = (double) data.get("avgRating");
    }
}
