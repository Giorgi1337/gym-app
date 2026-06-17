package com.gym.service;

import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import com.gym.validation.OnCreate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.gym.utils.NameUtils.normalize;

@Service
@Transactional
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final UsernameGenerator usernameGenerator;
    private final TrainingTypeDao trainingTypeDao;
    private final Validator validator;

    public TrainerService(TrainerDao trainerDao, UsernameGenerator usernameGenerator, TrainingTypeDao trainingTypeDao, Validator validator) {
        this.trainerDao = trainerDao;
        this.usernameGenerator = usernameGenerator;
        this.trainingTypeDao = trainingTypeDao;
        this.validator = validator;
    }

    public void save(Trainer trainer) {
        if (trainer.getUser() != null) {
            User user = trainer.getUser();
            user.setFirstName(normalize(user.getFirstName()));
            user.setLastName(normalize(user.getLastName()));
        }

        if (trainer.getSpecialization() != null) {
            trainer.getSpecialization().setTrainingTypeName(
                    StringUtils.normalizeSpace(trainer.getSpecialization().getTrainingTypeName())
            );
        }

        Set<ConstraintViolation<Trainer>> violations = validator.validate(trainer, OnCreate.class);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        User user = trainer.getUser();
        trainer.getUser().setUsername(usernameGenerator.generate(user.getFirstName(), user.getLastName()));
        trainer.getUser().setPassword(PasswordGenerator.generate());

        String specializationName = trainer.getSpecialization().getTrainingTypeName();
        TrainingType specialization = trainingTypeDao.findByName(specializationName);

        if (specialization == null) {
            throw new IllegalArgumentException("Specialization not found: " + specializationName);
        }

        trainer.setSpecialization(specialization);

        trainerDao.save(trainer);
        log.info("Created trainer with username: {}", trainer.getUser().getUsername());
    }
}