package com.gym.service;

import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.exception.AuthenticationException;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.security.RequiresAuthentication;
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

    @RequiresAuthentication
    @Transactional(readOnly = true)
    public Trainer findByUsername(String username) {
        log.info("Fetching trainer by username: {}", username);

        return trainerDao.findByUserName(username)
                .orElseThrow(() -> {
                    log.warn("Trainer not found: {}", username);
                    return new AuthenticationException("Trainer not found: " + username);
                });
    }

    @RequiresAuthentication
    public void changePassword(final String username, final String oldPassword, final String newPassword) {
        log.info("Changing password for trainer: {}", username);

        Trainer trainer = trainerDao.findByUserName(username)
                .orElseThrow(() -> {
                    log.warn("Trainee not found: {}", username);
                    return new IllegalArgumentException("Trainer not found: " + username);
                });

        User user = trainer.getUser();

        if (!user.getPassword().equals(oldPassword)) {
            log.warn("Password change failed for trainer: {} — old password does not match", username);
            throw new AuthenticationException("Old password does not match");
        }

        if (newPassword == null || newPassword.isBlank()) {
            log.warn("Password change failed for trainer: {} — new password is blank", username);
            throw new IllegalArgumentException("New password must not be blank");
        }

        user.setPassword(newPassword);
        log.info("Password changed successfully for trainer: {}", username);
    }

    @RequiresAuthentication
    public Trainer update(final String username, final Trainer updatedData) {
        log.info("Updating trainer: {}", username);

        Trainer trainer = findByUsername(username);

        String firstName = normalize(updatedData.getUser().getFirstName());
        String lastName = normalize(updatedData.getUser().getLastName());

        if (firstName.isBlank() || lastName.isBlank()) {
            log.warn("Update failed for trainer: {} — first or last name is blank", username);
            throw new IllegalArgumentException("First name and last name are required");
        }

        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);

        String newUsername = usernameGenerator.generate(firstName, lastName);
        trainer.getUser().setUsername(newUsername);

        if (updatedData.getSpecialization() != null) {
            String specializationName = StringUtils.normalizeSpace(
                    updatedData.getSpecialization().getTrainingTypeName()
            );

            TrainingType specialization = trainingTypeDao.findByName(specializationName);
            if (specialization == null) {
                log.warn("Update failed for trainer: {} — specialization not found: {}", username, specializationName);
                throw new IllegalArgumentException("Specialization not found: " + specializationName);
            }

            trainer.setSpecialization(specialization);
        }

        log.info("Trainer updated successfully: {} → new username: {}", username, newUsername);
        return trainer;
    }

    @RequiresAuthentication
    public void setActive(final String username, final boolean active) {
        log.info("Setting trainer: {} to {}", username, active ? "active" : "inactive");

        Trainer trainer = findByUsername(username);
        User user = trainer.getUser();

        if (user.getIsActive().equals(active)) {
            log.warn("Trainer: {} is already {}", username, active ? "active" : "inactive");
            throw new IllegalStateException("Trainer is already " + (active ? "active" : "inactive"));
        }

        user.setIsActive(active);
        log.info("Trainer: {} is now {}", username, active ? "active" : "inactive");
    }
}