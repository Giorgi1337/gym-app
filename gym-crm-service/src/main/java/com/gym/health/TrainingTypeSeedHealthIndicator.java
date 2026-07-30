package com.gym.health;

import com.gym.repository.TrainingTypeRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeSeedHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeSeedHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public @Nullable Health health() {
        long count = trainingTypeRepository.count();

        if (count == 0) {
            return Health.down()
                    .withDetail("reason", "No training types seeded")
                    .build();
        }

        return Health.up().withDetail("trainingTypeCount", count).build();
    }
}
