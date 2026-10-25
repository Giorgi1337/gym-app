package com.gym.health;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Component
public class DatabaseLatencyHealthIndicator implements HealthIndicator {

    private static final long DEGRADED_THRESHOLD_MS = 200;
    private static final int VALIDATION_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public DatabaseLatencyHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public @Nullable Health health() {
        long start = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(VALIDATION_TIMEOUT_SECONDS);
            long elapsedMs = System.currentTimeMillis() - start;

            if (!valid) {
                return Health.down()
                        .withDetail("reason", "Connection not valid")
                        .build();
            }

            DatabaseMetaData metaData = connection.getMetaData();
            Health.Builder builder = elapsedMs > DEGRADED_THRESHOLD_MS
                    ? Health.status("OUT_OF_SERVICE")
                    : Health.up();

            return builder
                    .withDetail("database", metaData.getDatabaseProductName())
                    .withDetail("responseTimesMs", elapsedMs)
                    .build();

        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
