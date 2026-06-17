package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.exception.AuthenticationException;
import com.gym.model.Trainee;
import com.gym.model.User;
import com.gym.security.RequiresAuthentication;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import com.gym.validation.OnCreate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static com.gym.utils.NameUtils.normalize;

@Service
@Transactional
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeDao traineeDao;
    private final UsernameGenerator usernameGenerator;
    private final Validator validator;

    public TraineeService(TraineeDao traineeDao, UsernameGenerator usernameGenerator, Validator validator) {
        this.traineeDao = traineeDao;
        this.usernameGenerator = usernameGenerator;
        this.validator = validator;
    }

    public void save(Trainee trainee) {
        if (trainee.getUser() != null) {
            User user = trainee.getUser();
            user.setFirstName(normalize(user.getFirstName()));
            user.setLastName(normalize(user.getLastName()));
        }

        Set<ConstraintViolation<Trainee>> violations = validator.validate(trainee, OnCreate.class);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        String username = usernameGenerator.generate(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName()
        );
        String password = PasswordGenerator.generate();

        trainee.getUser().setUsername(username);
        trainee.getUser().setPassword(password);

        traineeDao.save(trainee);
        log.info("Created trainee with username: {}", username);
    }

    @RequiresAuthentication
    @Transactional(readOnly = true)
    public Trainee findByUsername(final String username) {
        log.info("Fetching trainee by username: {}", username);

        return traineeDao.findByUserName(username)
                .orElseThrow(() -> {
                    log.warn("Trainee not found: {}", username);
                    return new IllegalArgumentException("Trainee not found: " + username);
                });
    }

    @RequiresAuthentication
    public void changePassword(final String username, final String oldPassword, final String newPassword) {
        log.info("Changing password for trainee: {}", username);

        Trainee trainee = traineeDao.findByUserName(username)
                .orElseThrow(() -> {
                    log.warn("Trainee not found: {}", username);
                    return new IllegalArgumentException("Trainee not found: " + username);
                });

        User user = trainee.getUser();

        if (!user.getPassword().equals(oldPassword)) {
            log.warn("Password change failed for trainee: {} — old password does not match", username);
            throw new AuthenticationException("Old password does not match");
        }

        if (newPassword == null || newPassword.isBlank()) {
            log.warn("Password change failed for trainee: {} — new password is blank", username);
            throw new IllegalArgumentException("New password must not be blank");
        }

        user.setPassword(newPassword);
        log.info("Password changed successfully for trainee: {}", username);
    }

    @RequiresAuthentication
    public Trainee update(final String username, final Trainee updatedData) {
        log.info("Updating trainee: {}", username);

        Trainee trainee = findByUsername(username);

        String firstName = normalize(updatedData.getUser().getFirstName());
        String lastName = normalize(updatedData.getUser().getLastName());

        if (firstName.isBlank() || lastName.isBlank()) {
            log.warn("Update failed for trainee: {} — first or last name is blank", username);
            throw new IllegalArgumentException("First name and last name are required");
        }

        if (updatedData.getDateOfBirth() != null && updatedData.getDateOfBirth().isAfter(LocalDate.now())) {
            log.warn("Update failed for trainee: {} — date of birth is in the future", username);
            throw new IllegalArgumentException("Date of birth must be in the past");
        }

        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);

        String newUsername = usernameGenerator.generate(firstName, lastName);
        trainee.getUser().setUsername(newUsername);

        trainee.setDateOfBirth(updatedData.getDateOfBirth());
        trainee.setAddress(updatedData.getAddress());

        log.info("Trainee updated successfully: {} → new username: {}", username, newUsername);
        return trainee;
    }

    @RequiresAuthentication
    public void setActive(final String username, final boolean active) {
        log.info("Setting trainee: {} to {}", username, active ? "active" : "inactive");

        Trainee trainee = findByUsername(username);
        User user = trainee.getUser();

        if (user.getIsActive().equals(active)) {
            log.warn("Trainee: {} is already {}", username, active ? "active" : "inactive");
            throw new IllegalStateException("Trainee is already " + (active ? "active" : "inactive"));
        }

        user.setIsActive(active);
        log.info("Trainee: {} is now {}", username, active ? "active" : "inactive");
    }

    @RequiresAuthentication
    public void deleteByUsername(final String username) {
        log.info("Deleting trainee: {}", username);

        Trainee trainee = findByUsername(username);
        traineeDao.delete(trainee);

        log.info("Trainee deleted successfully: {}", username);
    }

}