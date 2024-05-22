package uns.ac.rs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Map;

@Data
public class GeneralResponse<T> {
    public String message;
    public T data;

    public GeneralResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public GeneralResponse(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, T> dictionary = objectMapper.readValue(jsonString, new TypeReference<>() {});
            this.message = (String) dictionary.get("message");
            this.data = dictionary.get("data");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
