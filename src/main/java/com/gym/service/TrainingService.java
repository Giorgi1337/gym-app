package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.security.RequiresAuthentication;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final Validator validator;

    public TrainingService(TrainingDao trainingDao, TraineeDao traineeDao, TrainerDao trainerDao, TrainingTypeDao trainingTypeDao, Validator validator) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.validator = validator;
    }

    @RequiresAuthentication
    public void addTraining(String traineeUsername, String trainerUsername,
                            String trainingName, String trainingTypeName,
                            LocalDate trainingDate, int trainingDuration) {
        log.info("Adding training '{}' for trainee: {}", trainingName, traineeUsername);

        Trainee trainee = traineeDao.findByUserName(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found: " + traineeUsername));

        Trainer trainer = trainerDao.findByUserName(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found: " + trainerUsername));

        TrainingType trainingType = trainingTypeDao.findByName(trainingTypeName);
        if (trainingType == null) {
            throw new IllegalArgumentException("Training type not found: " + trainingTypeName);
        }

        if (trainingDate == null) {
            throw new IllegalArgumentException("Training date is required");
        }

        if (trainingName == null || trainingName.isBlank()) {
            throw new IllegalArgumentException("Training name is required");
        }

        if (trainingDuration < 1) {
            throw new IllegalArgumentException("Duration must be positive");
        }

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(trainingName)
                .trainingType(trainingType)
                .trainingDate(trainingDate)
                .trainingDuration(trainingDuration)
                .build();

        trainingDao.save(training);
        log.info("Training '{}' added successfully", trainingName);
    }

    @RequiresAuthentication
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String traineeUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String trainerName, String trainingType) {
        log.info("Fetching trainings for trainee: {}", traineeUsername);
        return trainingDao.findByTraineeUsername(traineeUsername, fromDate, toDate, trainerName, trainingType);
    }

    @RequiresAuthentication
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String trainerUsername,
                                              LocalDate fromDate, LocalDate toDate,
                                              String traineeName) {
        log.info("Fetching trainings for trainer: {}", trainerUsername);
        return trainingDao.findByTrainerUsername(trainerUsername, fromDate, toDate, traineeName);
    }
}