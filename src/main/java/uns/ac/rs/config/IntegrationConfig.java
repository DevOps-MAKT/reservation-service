package uns.ac.rs.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "integration")
public interface IntegrationConfig {

    String userServiceAPI();
    String accommodationServiceAPI();
    String notificationServiceAPI();
}
