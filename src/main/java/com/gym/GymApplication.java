package com.gym;

import com.gym.config.AppConfig;
import com.gym.facade.GymFacade;
import com.gym.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymApplication {

    private static final Logger log = LoggerFactory.getLogger(GymApplication.class);

    public static void main(String[] args) {

        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = ctx.getBean(GymFacade.class);

            // ─── Trainees ────────────────────────────────────────────────────────

            log.info("=== CREATE TRAINEES ===");
            Trainee t1 = facade.createTrainee(Trainee.builder()
                    .userId(100L)
                    .firstName("John")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1995, 3, 15))
                    .address("123 Main St")
                    .build());

            Trainee t2 = facade.createTrainee(Trainee.builder()
                    .userId(101L)
                    .firstName("John")
                    .lastName("Smith")
                    .dateOfBirth(LocalDate.of(1998, 7, 22))
                    .address("456 Oak Ave")
                    .build());

            log.info("=== FIND TRAINEE BY ID ===");
            facade.findTraineeById(100L).ifPresent(t -> log.info("Found: {}", t));

            log.info("=== UPDATE TRAINEE ===");
            t1.setAddress("999 New Address");
            t1.setActive(false);

            Trainee updated = facade.updateTrainee(t1);
            log.info("Updated: {}", updated);

            log.info("=== ALL TRAINEES ===");
            facade.findAllTrainees().forEach(t -> log.info("{}", t));

            log.info("=== DELETE TRAINEE ===");
            facade.deleteTrainee(101L);
            log.info("remaining: {}", facade.findAllTrainees().size());

            // ─── Trainers ────────────────────────────────────────────────────────

            log.info("=== CREATE TRAINERS ===");
            Trainer tr1 = facade.createTrainer(Trainer.builder()
                    .userId(200L)
                    .firstName("Jane")
                    .lastName("Doe")
                    .specialization("Yoga")
                    .build());

            Trainer tr2 = facade.createTrainer(Trainer.builder()
                    .userId(201L)
                    .firstName("Jane")
                    .lastName("Doe")
                    .specialization("Pilates")
                    .build());

            log.info("=== FIND TRAINER BY ID ===");
            facade.findTrainerById(200L).ifPresent(t -> log.info("Found: {}", t));

            log.info("=== UPDATE TRAINER ===");
            tr1.setSpecialization("CrossFit");
            tr1.setActive(false);

            Trainer updatedTrainer = facade.updateTrainer(tr1);
            log.info("Updated: {}", updatedTrainer);

            log.info("=== ALL TRAINERS ===");
            facade.findAllTrainers().forEach(t -> log.info("{}", t));

            // ─── Trainings ───────────────────────────────────────────────────────

            log.info("=== CREATE TRAININGS ===");
            Training training1 = facade.createTraining(Training.builder()
                    .traineeId(100L)
                    .trainerId(200L)
                    .trainingName("Morning Yoga Session")
                    .trainingType(TrainingType.YOGA)
                    .trainingDate(LocalDate.now())
                    .trainingDurationMinutes(60)
                    .build());

            Training training2 = facade.createTraining(Training.builder()
                    .traineeId(100L)
                    .trainerId(201L)
                    .trainingName("Evening Pilates")
                    .trainingType(TrainingType.PILATES)
                    .trainingDate(LocalDate.now().plusDays(1))
                    .trainingDurationMinutes(45)
                    .build());

            log.info("=== ALL TRAININGS ===");
            facade.findAllTrainings().forEach(t -> log.info("{}", t));

            log.info("=== TRAININGS BY TRAINER ===");
            facade.findTrainingsByTrainerId(200L).forEach(t -> log.info("{}", t));

            log.info("=== TRAININGS BY TRAINEE ===");
            facade.findTrainingsByTraineeId(100L).forEach(t -> log.info("{}", t));
        }
    }
}