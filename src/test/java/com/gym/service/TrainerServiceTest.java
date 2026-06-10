package com.gym.service;

import com.gym.dao.InMemoryDao;
import com.gym.model.Trainer;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TrainerServiceTest {

    private TrainerService trainerService;
    private InMemoryDao<Trainer, Long> trainerDao;
    private ConcurrentHashMap<Long, Trainer> storage;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    public void setup() {
        storage = new ConcurrentHashMap<>();
        trainerDao = new InMemoryDao<>(storage);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);

        trainerService = new TrainerService();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createWithGeneratedUsernameAndPassword() {
        Trainer trainer = buildTrainer(1L, "Alice", "Johnson", "Yoga");

        when(usernameGenerator.generate("Alice", "Johnson")).thenReturn("Alice.Johnson");
        when(passwordGenerator.generate()).thenReturn("pass123ABC");

        Trainer result = trainerService.create(trainer);

        assertThat(result.getUsername()).isEqualTo("Alice.Johnson");
        assertThat(result.isActive()).isTrue();
        assertThat(storage.get(1L)).isEqualTo(result);
    }

    @Test
    void createThrowsExceptionWhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerService.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createThrowsExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> trainerService.create(buildTrainer(null, "Alice", "Johnson", "Yoga")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createThrowsExceptionWhenTrainerAlreadyExists() {
        storage.put(1L, buildTrainer(1L, "Alice", "Johnson", "Yoga"));

        assertThatThrownBy(() -> trainerService.create(buildTrainer(1L, "Alice", "Johnson", "Yoga")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainer already exists with id: 1");
    }

    @Test
    void updateUpdatesAllowedFields() {
        storage.put(1L, buildTrainer(1L, "Alice", "Johnson", "Yoga"));

        Trainer updated = Trainer.builder()
                .userId(1L)
                .firstName("Alicia")
                .lastName("Johnstone")
                .specialization("Pilates")
                .build();

        Trainer result = trainerService.update(updated);

        assertThat(result.getFirstName()).isEqualTo("Alicia");
        assertThat(result.getLastName()).isEqualTo("Johnstone");
        assertThat(result.getSpecialization()).isEqualTo("Pilates");
        assertThat(storage.get(1L)).isEqualTo(result);
    }

    @Test
    void updateThrowsExceptionWhenTrainerNotFound() {
        assertThatThrownBy(() -> trainerService.update(buildTrainer(1L, "Alice", "Johnson", "Yoga")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainer not found with id: 1");
    }

    @Test
    void updateThrowsExceptionWhenTrainerIsNull() {
        assertThatThrownBy(() -> trainerService.update(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateThrowsExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> trainerService.update(buildTrainer(null, "Alice", "Johnson", "Yoga")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findByIdReturnsTrainerWhenExists() {
        Trainer trainer = buildTrainer(1L, "Alice", "Johnson", "Yoga");
        storage.put(1L, trainer);

        assertThat(trainerService.findById(1L)).isEqualTo(Optional.of(trainer));
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        assertThat(trainerService.findById(1L)).isEmpty();
    }

    @Test
    void findByIdThrowsExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> trainerService.findById(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findAllReturnsAllTrainers() {
        storage.put(1L, buildTrainer(1L, "Alice", "Johnson", "Yoga"));
        storage.put(2L, buildTrainer(2L, "Bob", "Williams", "CrossFit"));

        assertThat(trainerService.findAll()).hasSize(2);
    }

    @Test
    void findAllReturnsEmptyListWhenNoTrainers() {
        assertThat(trainerService.findAll()).isEmpty();
    }

    private Trainer buildTrainer(Long id, String firstName, String lastName, String specialization) {
        return Trainer.builder()
                .userId(id)
                .firstName(firstName)
                .lastName(lastName)
                .specialization(specialization)
                .build();
    }
}