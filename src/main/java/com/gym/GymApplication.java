package com.gym;

import com.gym.config.AppConfig;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.service.AuthenticationService;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymApplication {

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            AuthenticationService auth = context.getBean(AuthenticationService.class);

            Trainee trainee = Trainee.builder()
                    .user(User.builder()
                            .firstName("John ")
                            .lastName("Doe")
                            .isActive(true)
                            .build())
                    .dateOfBirth(LocalDate.of(1994, 12, 13))
                    .build();

            traineeService.save(trainee);

            Trainer trainer = Trainer.builder()
                    .user(User.builder()
                            .firstName("EQWEWQ")
                            .lastName("DSA  ")
                            .isActive(false)
                            .build())
                    .specialization(TrainingType
                            .builder()
                            .trainingTypeName(" boxing      ")
                            .build())
                    .build();

            trainerService.save(trainer);

            // Auth Trainee / Trainer
            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();

            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();

            auth.login(traineeUsername, traineePassword);
            auth.login(trainerUsername, trainerPassword);

            // Find Trainer / Trainer ByUsername
            traineeService.findByUsername(traineeUsername);
            trainerService.findByUsername(trainerUsername);

            // Change Password  Trainer / Trainee
            String newTraineePassword = "newPass456";
            String newTrainerPassword = "newPass987";
            traineeService.changePassword(traineeUsername, traineePassword, newTraineePassword);
            trainerService.changePassword(trainerUsername, trainerPassword, newTrainerPassword);

            // Update Trainee
            Trainee traineeUpdate = Trainee.builder()
                    .user(User.builder()
                            .firstName("Johnny")
                            .lastName("Doe")
                            .build())
                    .dateOfBirth(LocalDate.of(1990, 6, 15))
                    .address("456 New St")
                    .build();

            Trainee updatedTrainee = traineeService.update(traineeUsername, traineeUpdate);
            IO.println("Updated trainee username: " + updatedTrainee.getUser().getUsername());

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
            IO.println("Updated trainer username: " + updatedTrainer.getUser().getUsername());

            // setActive / deactivate
            traineeService.setActive(updatedTrainee.getUser().getUsername(), false);

            traineeService.setActive(updatedTrainee.getUser().getUsername(), true);

            // test setActive same status throws
            try {
                traineeService.setActive(updatedTrainee.getUser().getUsername(), true);
                IO.println("ERROR: should have thrown");
            } catch (IllegalStateException e) {
                IO.println("Caught expected: " + e.getMessage());
            }

            // deleteByUsername
            traineeService.deleteByUsername(updatedTrainee.getUser().getUsername());
            IO.println("Trainee deleted");

            // verify deleted
            try {
                traineeService.findByUsername(updatedTrainee.getUser().getUsername());
                IO.println("ERROR: should have thrown");
            } catch (IllegalArgumentException e) {
                IO.println("Caught expected: " + e.getMessage());
            }

            // logout and verify blocked
            auth.logout();
            try {
                traineeService.findByUsername(traineeUsername);
                System.out.println("ERROR: should have thrown");
            } catch (Exception e) {
                System.out.println("Unauthenticated access blocked: " + e.getMessage());
            }
        }
    }
}