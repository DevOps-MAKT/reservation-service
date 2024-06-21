package uns.ac.rs;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class MicroserviceCommunicator {

    public GeneralResponse processResponse(String apiUrl, String requestMethod, String authorizationHeader, String requestBody) {
        return this.sendRequest(apiUrl, requestMethod, authorizationHeader, requestBody);
    }
    private GeneralResponse sendRequest(String apiUrl, String requestMethod, String authorizationHeader, String requestBody) {
        StringBuilder response = new StringBuilder();
        int responseCode = 500;
        try {

            URL url = new URL(apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(requestMethod);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authorizationHeader);
            if ("POST".equalsIgnoreCase(requestMethod)) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            responseCode = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            connection.disconnect();

        } catch (IOException e) {
            return new GeneralResponse<>("", String.valueOf(responseCode));
        }
        return new GeneralResponse<>(response.toString());
    }

}
