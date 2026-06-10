package com.gym.utils;

import com.gym.dao.InMemoryDao;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UsernameGeneratorTest {

    private UsernameGenerator usernameGenerator;

    private ConcurrentHashMap<Long, Trainee> traineeStorage;
    private ConcurrentHashMap<Long, Trainer> trainerStorage;

    @BeforeEach
    void setUp() {
        traineeStorage = new ConcurrentHashMap<>();
        trainerStorage = new ConcurrentHashMap<>();

        usernameGenerator = new UsernameGenerator();
        usernameGenerator.setTraineeDao(new InMemoryDao<>(traineeStorage));
        usernameGenerator.setTrainerDao(new InMemoryDao<>(trainerStorage));
    }

    @Test
    void generateNormalizesUsername() {
        assertThat(usernameGenerator.generate("jOhN", "sMITh"))
                .isEqualTo("John.Smith");
    }

    @Test
    void generateWhenTraineeUserNameExists() {
        traineeStorage.put(1L, buildTrainee(1L, "John.Smith"));

        assertThat(usernameGenerator.generate("John", "Smith"))
                .isEqualTo("John.Smith1");
    }

    @Test
    void generateWhenTrainerUserNameExists() {
        trainerStorage.put(1L, buildTrainer(1L, "John.Smith"));

        assertThat(usernameGenerator.generate("John", "Smith"))
                .isEqualTo("John.Smith1");
    }

    @Test
    void generateWhenUserNameAndSerialExists() {
        traineeStorage.put(1L, buildTrainee(1L, "John.Smith"));
        trainerStorage.put(2L, buildTrainer(2L, "John.Smith1"));
        traineeStorage.put(3L, buildTrainee(3L, "John.Smith2"));

        assertThat(usernameGenerator.generate("John", "Smith"))
                .isEqualTo("John.Smith3");
    }

    @Test
    void generateThrowsExceptionWhenBlankOrNullInput() {
        assertThatThrownBy(() -> usernameGenerator.generate("", "Smith"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name must not be blank");

        assertThatThrownBy(() -> usernameGenerator.generate("John", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name must not be blank");

        assertThatThrownBy(() -> usernameGenerator.generate(null, "Smith"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name must not be blank");

        assertThatThrownBy(() -> usernameGenerator.generate("John", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name and last name must not be blank");
    }


    private Trainee buildTrainee(Long id, String username) {
        return Trainee.builder()
                .userId(id)
                .username(username)
                .firstName("John")
                .lastName("Smith")
                .build();
    }

    private Trainer buildTrainer(Long id, String username) {
        return Trainer.builder()
                .userId(id)
                .username(username)
                .firstName("John")
                .lastName("Smith")
                .build();
    }

}