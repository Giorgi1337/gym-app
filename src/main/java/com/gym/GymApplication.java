package com.gym;

import com.gym.config.AppConfig;
import com.gym.model.*;
import com.gym.service.AuthenticationService;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import com.gym.service.TrainingService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymApplication {

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            AuthenticationService auth = context.getBean(AuthenticationService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            // --- create trainee ---
            Trainee trainee = Trainee.builder()
                    .user(User.builder()
                            .firstName("John ")
                            .lastName("Doe")
                            .isActive(true)
                            .build())
                    .dateOfBirth(LocalDate.of(1994, 12, 13))
                    .address("123 Main St")
                    .build();

            traineeService.save(trainee);
            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();
            System.out.println("Created trainee: " + traineeUsername);

            // --- create trainer ---
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

            trainerService.save(trainer);
            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();
            System.out.println("Created trainer: " + trainerUsername);

            // --- login ---
            auth.login(traineeUsername, traineePassword);
            System.out.println("Logged in as: " + traineeUsername);

            // --- find by username ---
            Trainee foundTrainee = traineeService.findByUsername(traineeUsername);
            System.out.println("Found trainee: " + foundTrainee.getUser().getFirstName()
                    + " " + foundTrainee.getUser().getLastName());

            Trainer foundTrainer = trainerService.findByUsername(trainerUsername);
            System.out.println("Found trainer: " + foundTrainer.getUser().getFirstName()
                    + " " + foundTrainer.getUser().getLastName());

            // --- change password ---
            String newTraineePassword = "newPass456";
            String newTrainerPassword = "newPass987";
            traineeService.changePassword(traineeUsername, traineePassword, newTraineePassword);
            System.out.println("Trainee password changed");

            trainerService.changePassword(trainerUsername, trainerPassword, newTrainerPassword);
            System.out.println("Trainer password changed");

            // --- test wrong old password ---
            try {
                traineeService.changePassword(traineeUsername, "wrongPass", "anotherPass");
                System.out.println("ERROR: should have thrown");
            } catch (Exception e) {
                System.out.println("Caught expected: " + e.getMessage());
            }

            // --- update trainee ---
            Trainee traineeUpdate = Trainee.builder()
                    .user(User.builder()
                            .firstName("Johnny")
                            .lastName("Doe")
                            .build())
                    .dateOfBirth(LocalDate.of(1990, 6, 15))
                    .address("456 New St")
                    .build();

            Trainee updatedTrainee = traineeService.update(traineeUsername, traineeUpdate);
            System.out.println("Updated trainee username: " + updatedTrainee.getUser().getUsername());

            // --- update trainer ---
            Trainer trainerUpdate = Trainer.builder()
                    .user(User.builder()
                            .firstName("Nikoloz")
                            .lastName("Doe")
                            .build())
                    .specialization(TrainingType.builder()
                            .trainingTypeName("Yoga")
                            .build())
                    .build();

            Trainer updatedTrainer = trainerService.update(trainerUsername, trainerUpdate);
            System.out.println("Updated trainer username: " + updatedTrainer.getUser().getUsername());

            String updatedTraineeUsername = updatedTrainee.getUser().getUsername();
            String updatedTrainerUsername = updatedTrainer.getUser().getUsername();

            // --- setActive ---
            traineeService.setActive(updatedTraineeUsername, false);
            System.out.println("Trainee deactivated");

            traineeService.setActive(updatedTraineeUsername, true);
            System.out.println("Trainee activated");

            // --- test setActive same status ---
            try {
                traineeService.setActive(updatedTraineeUsername, true);
                System.out.println("ERROR: should have thrown");
            } catch (IllegalStateException e) {
                System.out.println("Caught expected: " + e.getMessage());
            }

            trainerService.setActive(updatedTrainerUsername, false);
            System.out.println("Trainer deactivated");

            trainerService.setActive(updatedTrainerUsername, true);
            System.out.println("Trainer activated");

            // --- get unassigned trainers ---
            List<Trainer> unassigned = traineeService.getUnassignedTrainers(updatedTraineeUsername);
            System.out.println("Unassigned trainers: " + unassigned.size());
            unassigned.forEach(t -> System.out.println("  - " + t.getUser().getUsername()));

            // --- update trainers list ---
            traineeService.updateTrainersList(updatedTraineeUsername, List.of(updatedTrainerUsername));
            System.out.println("Trainers list updated for: " + updatedTraineeUsername);

            // --- add training ---
            trainingService.addTraining(
                    updatedTraineeUsername,
                    updatedTrainerUsername,
                    "Morning Yoga Session",
                    "Yoga",
                    LocalDate.now(),
                    60
            );
            System.out.println("Training added");

            // --- add another training for filter testing ---
            trainingService.addTraining(
                    updatedTraineeUsername,
                    updatedTrainerUsername,
                    "Evening Yoga Session",
                    "Yoga",
                    LocalDate.now().minusDays(5),
                    45
            );
            System.out.println("Second training added");

            // --- get trainee trainings no filter ---
            List<Training> allTraineeTrainings = trainingService.getTraineeTrainings(
                    updatedTraineeUsername, null, null, null, null
            );
            System.out.println("All trainee trainings: " + allTraineeTrainings.size());
            allTraineeTrainings.forEach(t ->
                    System.out.println("  - " + t.getTrainingName() + " on " + t.getTrainingDate()));

            // --- get trainee trainings with date filter ---
            List<Training> filteredByDate = trainingService.getTraineeTrainings(
                    updatedTraineeUsername,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1),
                    null,
                    null
            );
            System.out.println("Trainee trainings (last 1 day): " + filteredByDate.size());

            // --- get trainee trainings with trainer filter ---
            List<Training> filteredByTrainer = trainingService.getTraineeTrainings(
                    updatedTraineeUsername, null, null, updatedTrainerUsername, null
            );
            System.out.println("Trainee trainings (by trainer): " + filteredByTrainer.size());

            // --- get trainee trainings with type filter ---
            List<Training> filteredByType = trainingService.getTraineeTrainings(
                    updatedTraineeUsername, null, null, null, "Yoga"
            );
            System.out.println("Trainee trainings (Yoga): " + filteredByType.size());

            // --- get trainer trainings no filter ---
            List<Training> allTrainerTrainings = trainingService.getTrainerTrainings(
                    updatedTrainerUsername, null, null, null
            );
            System.out.println("All trainer trainings: " + allTrainerTrainings.size());

            // --- get trainer trainings with trainee filter ---
            List<Training> trainerFilteredByTrainee = trainingService.getTrainerTrainings(
                    updatedTrainerUsername, null, null, updatedTraineeUsername
            );
            System.out.println("Trainer trainings (by trainee): " + trainerFilteredByTrainee.size());

            // --- delete trainee ---
            traineeService.deleteByUsername(updatedTraineeUsername);
            System.out.println("Trainee deleted");


            // --- verify deleted ---
            try {
                traineeService.findByUsername(updatedTraineeUsername);
                System.out.println("ERROR: should have thrown");
            } catch (IllegalArgumentException e) {
                System.out.println("Caught expected: " + e.getMessage());
            }

            // --- logout and verify blocked ---
            auth.logout();
            try {
                trainerService.findByUsername(updatedTrainerUsername);
                System.out.println("ERROR: should have thrown");
            } catch (Exception e) {
                System.out.println("Unauthenticated access blocked: " + e.getMessage());
            }
        }
    }
}