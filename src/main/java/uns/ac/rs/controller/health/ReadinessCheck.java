package uns.ac.rs.controller.health;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Readiness
@ApplicationScoped
public class ReadinessCheck implements HealthCheck {

    @Inject
    DataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return HealthCheckResponse.up("Readiness check - Database is reachable");
            } else {
                return HealthCheckResponse.down("Readiness check - Database is not reachable");
            }
        } catch (SQLException e) {
            return HealthCheckResponse.down("Readiness check - Database is not reachable");
        }
    }
}