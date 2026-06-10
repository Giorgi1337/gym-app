package com.gym.service;

import com.gym.dao.Dao;
import com.gym.model.Trainer;
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
public class TrainerService {

    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private Dao<Trainer, Long> trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(Dao<Trainer, Long> trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer create(Trainer trainer) {
        Validate.notNull(trainer, "Trainer must not be null");
        Validate.notNull(trainer.getUserId(), "Trainer userId must not be null");

        if (trainerDao.exists(trainer.getUserId())) {
            throw new IllegalStateException(
                    "Trainer already exists with id: " + trainer.getUserId());
        }

        trainer.setUsername(usernameGenerator.generate(
                trainer.getFirstName(),
                trainer.getLastName()
        ));
        trainer.setPassword(passwordGenerator.generate());
        trainer.setActive(true);

        trainerDao.save(trainer.getUserId(), trainer);
        log.info("Created trainer id = {}, username = {}", trainer.getUserId(), trainer.getUsername());
        return trainer;
    }

    public Trainer update(Trainer trainer) {
        Validate.notNull(trainer, "Trainer must not be null");
        Validate.notNull(trainer.getUserId(), "Trainer userId must not be null");

        Long id = trainer.getUserId();

        if (!trainerDao.exists(id)) {
            throw new IllegalStateException("Trainer not found with id: " + id);
        }

        Trainer existing = trainerDao.findById(id);
        existing.setFirstName(trainer.getFirstName());
        existing.setLastName(trainer.getLastName());
        existing.setSpecialization(trainer.getSpecialization());

        trainerDao.save(id, existing);
        log.info("Updated trainer id = {}", id);
        return existing;
    }

    public Optional<Trainer> findById(Long id) {
        Validate.notNull(id, "Trainer id must not be null");
        log.debug("Finding trainer id = {}", id);
        return Optional.ofNullable(trainerDao.findById(id));
    }

    public List<Trainer> findAll() {
        log.debug("Finding all trainers");
        return trainerDao.findAll();
    }
}