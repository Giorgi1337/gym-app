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

        }
    }
}