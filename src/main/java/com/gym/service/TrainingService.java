package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.dto.training.AddTrainingRequest;
import com.gym.dto.training.TraineeTrainingResponse;
import com.gym.dto.training.TrainerTrainingResponse;
import com.gym.dto.training.TrainingTypeResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainingMapper;
import com.gym.mapper.TrainingTypeMapper;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
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

    public TrainingService(TrainingDao trainingDao, TraineeDao traineeDao, TrainerDao trainerDao, TrainingTypeDao trainingTypeDao) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
    }

    @Transactional(readOnly = true)
    public List<TraineeTrainingResponse> getTraineeTrainings(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType) {

        traineeDao.getProfile(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + username));

        return trainingDao.findByTraineeUsername(username, fromDate, toDate, trainerName, trainingType)
                .stream()
                .map(TrainingMapper::toTraineeView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerTrainingResponse> getTrainerTrainings(
            String username,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        trainerDao.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + username));

        return trainingDao.findByTrainerUsername(username, fromDate, toDate, traineeName)
                .stream()
                .map(TrainingMapper::toTrainerView)
                .toList();
    }

    @Transactional
    public void addTraining(String trainerUsername, String traineeUsername, AddTrainingRequest request) {
        Trainer trainer = trainerDao.findByUserName(trainerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer not found: " + trainerUsername));

        Trainee trainee = traineeDao.getProfile(traineeUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee not found: " + traineeUsername));

        Training training = TrainingMapper.toEntity(request, trainee, trainer);
        trainingDao.save(training);

        log.info("Added training '{}' for trainer={} trainee={}",
                request.trainingName(), trainerUsername, traineeUsername);
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getTrainingTypes() {
        return trainingTypeDao.findAll().stream()
                .map(TrainingTypeMapper::toResponse)
                .toList();
    }
}