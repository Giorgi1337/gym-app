package com.gym.service;

import com.gym.dao.Dao;
import com.gym.model.Trainee;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TraineeService {

    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private Dao<Trainee, Long> traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(Dao<Trainee, Long> traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee create(Trainee trainee) {
        Validate.notNull(trainee, "Trainee must not be null");
        Validate.notNull(trainee.getUserId(), "Trainee userId must not be null");

        if (traineeDao.exists(trainee.getUserId())) {
            throw new IllegalStateException(
                    "Trainee already exists with id: " + trainee.getUserId());
        }

        trainee.setUsername(usernameGenerator.generate(
                trainee.getFirstName(),
                trainee.getLastName()
        ));

        trainee.setPassword(passwordGenerator.generate());
        trainee.setActive(true);

        traineeDao.save(trainee.getUserId(), trainee);
        log.info("Created trainee id = {}, username = {}", trainee.getUserId(), trainee.getUsername());
        return trainee;
    }

    public Trainee update(Trainee trainee) {
        Validate.notNull(trainee, "Trainee must not be null");
        Validate.notNull(trainee.getUserId(), "Trainee userId must not be null");

        Long id = trainee.getUserId();

        if (!traineeDao.exists(id)) {
            throw new IllegalStateException("Trainee not found with id: " + id);
        }

        Trainee existing = traineeDao.findById(id);
        existing.setFirstName(trainee.getFirstName());
        existing.setLastName(trainee.getLastName());
        existing.setDateOfBirth(trainee.getDateOfBirth());
        existing.setAddress(trainee.getAddress());

        traineeDao.save(id, existing);
        log.info("Updated trainee id = {}", id);
        return existing;
    }

    public void delete(Long id) {
        Validate.notNull(id, "Trainee id must not be null");

        if (!traineeDao.exists(id)) {
            throw new IllegalStateException("Trainee not found with id: " + id);
        }

        traineeDao.delete(id);
        log.info("Deleted trainee id = {}", id);
    }

    public Optional<Trainee> findById(Long id) {
        Validate.notNull(id, "Trainee id must not be null");
        log.debug("Finding trainee id = {}", id);
        return Optional.ofNullable(traineeDao.findById(id));
    }

    public List<Trainee> findAll() {
        log.debug("Finding all trainees");
        return traineeDao.findAll();
    }
}