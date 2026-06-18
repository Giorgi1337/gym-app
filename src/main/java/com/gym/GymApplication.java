package com.gym;

import com.gym.config.AppConfig;
import com.gym.facade.GymFacade;
import com.gym.model.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymApplication {

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade gym = context.getBean(GymFacade.class);

            // Register trainee
            Trainee trainee = Trainee.builder()
                    .user(User.builder()
                            .firstName("John ")
                            .lastName("Doe")
                            .isActive(true)
                            .build())
                    .dateOfBirth(LocalDate.of(1994, 12, 13))
                    .address("123 Main St")
                    .build();

            gym.registerTrainee(trainee);

            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();

            // Register trainer
            Trainer trainer = Trainer.builder()
                    .user(User.builder()
                            .firstName("Nika")
                            .lastName("Doe")
                            .isActive(true)
                            .build())
                    .specialization(TrainingType.builder()
                            .trainingTypeName("Boxing")
                            .build())
                    .build();

            gym.registerTrainer(trainer);

            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();

            // Login as trainee
            gym.login(traineeUsername, traineePassword);
            System.out.println("Logged in as: " + traineeUsername);

            // Fetch trainee profile
            Trainee foundTrainee = gym.getTrainee(traineeUsername);
            System.out.println("Found trainee: " + foundTrainee.getUser().getFirstName()
                    + " " + foundTrainee.getUser().getLastName());

            // Get unassigned trainers
            List<Trainer> unassigned = gym.getUnassignedTrainers(traineeUsername);
            System.out.println("Unassigned trainers: " + unassigned.size());

            // Assign trainer to trainee
            gym.updateTraineeTrainersList(traineeUsername, List.of(trainerUsername));
            System.out.println("Assigned trainer: " + trainerUsername);

            gym.addTraining(traineeUsername, trainerUsername,
                    "Morning Cardio", "Cardio",
                    LocalDate.now(), 60);
            System.out.println("Training added");

            // Fetch trainee trainings
            List<Training> traineeTrainings = gym.getTraineeTrainings(traineeUsername, null, null, null, null);
            System.out.println("Trainee trainings: " + traineeTrainings.size());

            // login as trainer
            gym.login(trainerUsername, trainerPassword);
            System.out.println("Logged in as: " + trainerUsername);

            // Fetch trainer trainings
            List<Training> trainerTrainings = gym.getTrainerTrainings(
                    trainerUsername, null, null, null);
            System.out.println("Trainer trainings: " + trainerTrainings.size());

            // Change trainer password
            gym.changeTrainerPassword(trainerUsername, trainerPassword, "newSecurePass123");
            System.out.println("Trainer password changed");

            // Deactivate trainee
            gym.logout();
            gym.login(traineeUsername, traineePassword);
            gym.setTraineeActive(traineeUsername, false);
            System.out.println("Trainee deactivated");
        }
    }
}