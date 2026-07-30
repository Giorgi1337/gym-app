package com.gym.health;

import com.gym.repository.TrainerRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ActiveTrainerAvailabilityHealthIndicator implements HealthIndicator {

    private final TrainerRepository trainerRepository;

    public ActiveTrainerAvailabilityHealthIndicator(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    public @Nullable Health health() {
        long activeTrainers = trainerRepository.countByUser_IsActiveTrue();

        if (activeTrainers == 0) {
            return Health.status("OUT_OF_SERVICE")
                    .withDetail("reason", "No active trainers available")
                    .build();
        }

        return Health.up().withDetail("activeTrainers", activeTrainers).build();
    }
}
