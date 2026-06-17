package com.gym.service;

import com.gym.dao.TraineeDao;
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

}