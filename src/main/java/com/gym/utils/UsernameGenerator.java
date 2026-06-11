package com.gym.utils;

import com.gym.dao.Dao;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UsernameGenerator {

    private Dao<Trainer, Long> trainerDao;
    private Dao<Trainee, Long> traineeDao;

    @Autowired
    public void setTrainerDao(Dao<Trainer, Long> trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(Dao<Trainee, Long> traineeDao) {
        this.traineeDao = traineeDao;
    }

    public String generate(String firstName, String lastName) {
        if (StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)) {
            throw new IllegalArgumentException("First name and last name must not be blank");
        }

        // Normalize casing: "john" -> "John", "SMITH" -> "Smith"
        String normalizedFirst = StringUtils.capitalize(firstName.toLowerCase());
        String normalizedLast = StringUtils.capitalize(lastName.toLowerCase());

        // Build base: "John.Smith"
        String base = StringUtils.joinWith(".", normalizedFirst, normalizedLast);

        // Collect all existing usernames from both storages
        Set<String> existingUsernames = collectAllUsernames();

        if (!existingUsernames.contains(base)) {
            return base;
        }

        // Find next available serial suffix: John.Smith1, John.Smith2, ...
        int serial = 1;
        while (existingUsernames.contains(base + serial)) {
            serial++;
        }
        return base + serial;
    }

    private Set<String> collectAllUsernames() {
        return Stream.concat(
                        trainerDao.findAll().stream().map(User::getUsername),
                        traineeDao.findAll().stream().map(User::getUsername)
                )
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}

