package com.gym.facade;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.service.AuthenticationService;
import com.gym.service.TraineeService;
import com.gym.service.TrainerService;
import com.gym.service.TrainingService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {

    private final AuthenticationService authService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(AuthenticationService authService, TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.authService = authService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Auth
    public void login(String username, String password) {
        authService.login(username, password);
    }

    public void logout() {
        authService.logout();
    }

    // Trainee
    public void registerTrainee(Trainee trainee) {
        traineeService.save(trainee);
    }

    public Trainee getTrainee(String username) {
        return traineeService.findByUsername(username);
    }

    public Trainee updateTrainee(String username, Trainee updatedData) {
        return traineeService.update(username, updatedData);
    }

    public void deleteTrainee(String username) {
        traineeService.deleteByUsername(username);
    }

    public void changeTraineePassword(String username, String oldPassword, String newPassword) {
        traineeService.changePassword(username, oldPassword, newPassword);
    }

    public void setTraineeActive(String username, boolean active) {
        traineeService.setActive(username, active);
    }

    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return traineeService.getUnassignedTrainers(traineeUsername);
    }

    public void updateTraineeTrainersList(String traineeUsername, List<String> trainerUsernames) {
        traineeService.updateTrainersList(traineeUsername, trainerUsernames);
    }

    // Trainer
    public void registerTrainer(Trainer trainer) {
        trainerService.save(trainer);
    }

    public Trainer getTrainer(String username) {
        return trainerService.findByUsername(username);
    }

    public Trainer updateTrainer(String username, Trainer updatedData) {
        return trainerService.update(username, updatedData);
    }

    public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
        trainerService.changePassword(username, oldPassword, newPassword);
    }

    public void setTrainerActive(String username, boolean active) {
        trainerService.setActive(username, active);
    }

    // Training
    public void addTraining(String traineeUsername, String trainerUsername,
                            String trainingName, String trainingTypeName,
                            LocalDate trainingDate, int trainingDuration) {
        trainingService.addTraining(
                traineeUsername, trainerUsername,
                trainingName, trainingTypeName,
                trainingDate, trainingDuration
        );
    }

    public List<Training> getTraineeTrainings(String traineeUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingType) {
        return trainingService.getTraineeTrainings(
                traineeUsername, fromDate, toDate, trainerName, trainingType
        );
    }

    public List<Training> getTrainerTrainings(String trainerUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        return trainingService.getTrainerTrainings(
                trainerUsername, fromDate, toDate, traineeName
        );
    }
}
