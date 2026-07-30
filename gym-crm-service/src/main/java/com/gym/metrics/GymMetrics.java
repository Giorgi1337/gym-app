package com.gym.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsAdded;
    private final Counter loginSuccess;
    private final Counter loginFailure;
    private final Timer trainingQueryTimer;

    public GymMetrics(MeterRegistry registry) {
        this.traineeRegistrations = Counter.builder("gym.registrations")
                .tag("role", "trainee")
                .description("Number of user registrations")
                .register(registry);

        this.trainerRegistrations = Counter.builder("gym.registrations")
                .tag("role", "trainer")
                .description("Number of user registrations")
                .register(registry);

        this.trainingsAdded = Counter.builder("gym.trainings.added")
                .description("Number of trainings scheduled")
                .register(registry);

        this.loginSuccess = Counter.builder("gym.auth.attempts")
                .tag("result", "success")
                .description("Login attempts")
                .register(registry);

        this.loginFailure = Counter.builder("gym.auth.attempts")
                .tag("result", "failure")
                .description("Login attempts")
                .register(registry);

        this.trainingQueryTimer = Timer.builder("gym.trainings.query")
                .description("Time to run training list queries")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }

    public void incrementTraineeRegistration() {
        traineeRegistrations.increment();
    }

    public void incrementTrainerRegistration() {
        trainerRegistrations.increment();
    }

    public void incrementTrainingAdded() {
        trainingsAdded.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccess.increment();
    }

    public void incrementLoginFailure() {
        loginFailure.increment();
    }

    public Timer trainingQueryTimer() {
        return trainingQueryTimer;
    }
}