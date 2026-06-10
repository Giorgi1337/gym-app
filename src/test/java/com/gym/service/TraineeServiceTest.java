package com.gym.service;

import com.gym.dao.InMemoryDao;
import com.gym.model.Trainee;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TraineeServiceTest {

    private TraineeService traineeService;
    private InMemoryDao<Trainee, Long> traineeDao;
    private ConcurrentHashMap<Long, Trainee> storage;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @BeforeEach
    public void setup() {
        storage = new ConcurrentHashMap<>();
        traineeDao = new InMemoryDao<>(storage);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);

        traineeService = new TraineeService();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void createWithGeneratedUserNameAndPassword() {
        Trainee trainee = buildTrainee(1L, "John", "Smith");

        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("pass123ABC");

        Trainee result = traineeService.create(trainee);

        assertThat(result.getUsername()).isEqualTo("John.Smith");
        assertThat(result.isActive()).isTrue();
        assertThat(storage.get(1L)).isEqualTo(result);
    }

    @Test
    void createThrowsExceptionWhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeService.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createThrowsExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> traineeService.create(buildTrainee(null, "John", "Smith")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createThrowsExceptionWhenTraineeAlreadyExists() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));

        assertThatThrownBy(() -> traineeService.create(buildTrainee(1L, "John", "Smith")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainee already exists with id: 1");
    }

    @Test
    void updateUpdatesAllowedFields() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));

        Trainee updated = Trainee.builder()
                .userId(1L)
                .firstName("Johnny")
                .lastName("Smithson")
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .address("New Address")
                .build();

        Trainee result = traineeService.update(updated);

        assertThat(result.getFirstName()).isEqualTo("Johnny");
        assertThat(result.getLastName()).isEqualTo("Smithson");
        assertThat(result.getAddress()).isEqualTo("New Address");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 1, 1));
        assertThat(storage.get(1L)).isEqualTo(result);
    }

    @Test
    void updateThrowsExceptionWhenTraineeNotFound() {
        assertThatThrownBy(() -> traineeService.update(buildTrainee(1L, "John", "Smith")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainee not found with id: 1");
    }

    @Test
    void updateThrowsExceptionWhenTraineeIsNull() {
        assertThatThrownBy(() -> traineeService.update(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateThrowsExceptionWhenUserIdIsNull() {
        assertThatThrownBy(() -> traineeService.update(buildTrainee(null, "John", "Smith")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteRemovesTraineeWhenExists() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));

        traineeService.delete(1L);

        assertThat(storage.get(1L)).isNull();
    }

    @Test
    void deleteThrowsExceptionWhenTraineeNotFound() {
        assertThatThrownBy(() -> traineeService.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Trainee not found with id: 1");
    }

    @Test
    void deleteThrowsExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> traineeService.delete(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findByIdReturnsTraineeWhenExists() {
        Trainee trainee = buildTrainee(1L, "John", "Smith");
        storage.put(1L, trainee);

        assertThat(traineeService.findById(1L)).isEqualTo(Optional.of(trainee));
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        assertThat(traineeService.findById(1L)).isEmpty();
    }

    @Test
    void findByIdThrowsExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> traineeService.findById(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void findAllReturnsAllTrainees() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));
        storage.put(2L, buildTrainee(2L, "Jane", "Doe"));

        assertThat(traineeService.findAll()).hasSize(2);
    }

    @Test
    void findAllReturnsEmptyListWhenNoTrainees() {
        assertThat(traineeService.findAll()).isEmpty();
    }

    private Trainee buildTrainee(Long id, String firstName, String lastName) {
        return Trainee.builder()
                .userId(id)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}
