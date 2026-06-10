package com.gym.service;

import com.gym.dao.Dao;
import com.gym.model.Training;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private Dao<Training, Long> trainingDao;
    private Map<Long, Training> trainingStorage;

    private long sequence = 1L;

    @Autowired
    public void setTrainingDao(Dao<Training, Long> trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTrainingStorage(Map<Long, Training> trainingStorage) {
        this.trainingStorage = trainingStorage;
    }

    @PostConstruct
    public void initSequence() {
        if (!trainingStorage.isEmpty()) {
            sequence = trainingStorage.keySet().stream()
                    .max(Long::compareTo)
                    .orElse(0L) + 1;
            log.info("Training sequence initialized to {}", sequence);
        }
    }

    public Training create(Training training) {
        Validate.notNull(training, "Training must not be null");
        Validate.notNull(training.getTrainerId(), "TrainerId must not be null");
        Validate.notNull(training.getTrainingName(), "TrainingName must not be null");

        Long id = sequence++;

        trainingDao.save(id, training);
        log.info("Created training id = {}, trainerId = {}, traineeId = {}, name = {}, type = {}, date = {}, duration = {} min",
                id,
                training.getTrainerId(),
                training.getTraineeId(),
                training.getTrainingName(),
                training.getTrainingType(),
                training.getTrainingDate(),
                training.getTrainingDurationMinutes()
        );
        return training;
    }

    public List<Training> findAll() {
        log.debug("Fetching all trainings");
        return trainingDao.findAll();
    }

    public List<Training> findByTrainerId(Long trainerId) {
        Validate.notNull(trainerId, "TrainerId must not be null");
        log.debug("Finding trainings by trainerId={}", trainerId);
        return trainingDao.findAll().stream()
                .filter(t -> trainerId.equals(t.getTrainerId()))
                .toList();
    }

    public List<Training> findByTraineeId(Long traineeId) {
        Validate.notNull(traineeId, "TraineeId must not be null");
        log.debug("Finding trainings by traineeId={}", traineeId);
        return trainingDao.findAll().stream()
                .filter(t -> traineeId.equals(t.getTraineeId()))
                .toList();
    }
}