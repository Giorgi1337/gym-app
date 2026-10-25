package com.gym.metrics;

import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class GymGaugeMetrics {

    private final MeterRegistry registry;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public GymGaugeMetrics(MeterRegistry registry, TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.registry = registry;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @PostConstruct
    void registerGauges() {
        Gauge.builder("gym.trainees.total", traineeRepository, TraineeRepository::count)
                .description("Total number of trainees")
                .register(registry);

        Gauge.builder("gym.trainers.total", trainerRepository, TrainerRepository::count)
                .description("Total number of trainers")
                .register(registry);

        Gauge.builder("gym.trainers.active", trainerRepository, TrainerRepository::countByUser_IsActiveTrue)
                .description("Number of active trainers")
                .register(registry);
    }
}