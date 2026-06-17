package com.gym.service;

import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.exception.AuthenticationException;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import jakarta.validation.ConstraintViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainerServiceTest {

    private TrainerDao trainerDao;
    private TrainerService trainerService;
    private UsernameGenerator usernameGenerator;
    private TrainingTypeDao trainingTypeDao;
    private MockedStatic<PasswordGenerator> passwordGenerator;
    private Validator validator;

    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        trainerDao = mock(TrainerDao.class);
        usernameGenerator = mock(UsernameGenerator.class);
        trainingTypeDao = mock(TrainingTypeDao.class);
        trainerService = new TrainerService(trainerDao, usernameGenerator, trainingTypeDao, validator);
        passwordGenerator = mockStatic(PasswordGenerator.class);
    }

    @AfterEach
    void tearDown() {
        passwordGenerator.close();
    }

    @Test
    void saveSetsUsernameAndPassword() {
        Trainer trainer = buildTrainer("John", "Smith", "Boxing");
        setupGenerators("John", "Smith");
        when(trainingTypeDao.findByName("Boxing")).thenReturn(boxing());

        trainerService.save(trainer);

        assertThat(trainer.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(trainer.getUser().getPassword()).isEqualTo("pass123ABC");
    }

    @Test
    void saveNormalizesNames() {
        Trainer trainer = buildTrainer("john", "smith", "Boxing");
        setupGenerators("John", "Smith");
        when(trainingTypeDao.findByName("Boxing")).thenReturn(boxing());

        trainerService.save(trainer);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith");
    }

    @Test
    void saveLooksUpSpecializationByName() {
        Trainer trainer = buildTrainer("John", "Smith", "Boxing");
        setupGenerators("John", "Smith");
        TrainingType boxing = boxing();
        when(trainingTypeDao.findByName("Boxing")).thenReturn(boxing);

        trainerService.save(trainer);

        assertThat(trainer.getSpecialization()).isEqualTo(boxing);
    }

    @Test
    void saveThrowsWhenSpecializationNotFoundInDb() {
        Trainer trainer = buildTrainer("John", "Smith", "Unknown");
        setupGenerators("John", "Smith");
        when(trainingTypeDao.findByName("Unknown")).thenReturn(null);

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Specialization not found: Unknown");
    }

    @Test
    void saveThrowsWhenUserIsNull() {
        Trainer trainer = buildTrainer("John", "Smith", "Boxing");
        trainer.setUser(null);

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("User is required");
    }

    @Test
    void saveThrowsWhenSpecializationIsNull() {
        Trainer trainer = buildTrainer("John", "Smith", "Boxing");
        trainer.setSpecialization(null);

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Specialization is required");
    }

    @Test
    void saveThrowsWhenFirstNameIsBlank() {
        Trainer trainer = buildTrainer("", "Smith", "Boxing");

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("First name is required");
    }

    @Test
    void saveThrowsWhenLastNameIsBlank() {
        Trainer trainer = buildTrainer("John", "", "Boxing");

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Last name is required");
    }

    @Test
    void saveThrowsWhenFirstNameHasInvalidCharacters() {
        Trainer trainer = buildTrainer("J0hn!", "Smith", "Boxing");

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("First name contains invalid characters");
    }

    @Test
    void saveThrowsWhenLastNameHasInvalidCharacters() {
        Trainer trainer = buildTrainer("John", "Sm1th@", "Boxing");

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Last name contains invalid characters");
    }

    @Test
    void saveThrowsWhenIsActiveIsNull() {
        Trainer trainer = buildTrainer("John", "Smith", "Boxing");
        trainer.getUser().setIsActive(null);

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Active status is required");
    }

    @Test
    void saveThrowsWhenSpecializationNameIsBlank() {
        Trainer trainer = buildTrainer("John", "Smith", "");

        assertThatThrownBy(() -> trainerService.save(trainer))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Training type name is required");
    }

    @Test
    void findByUsernameReturnsTrainer() {
        Trainer trainer = buildTrainer("John", "Smith", "Boxing");
        setupGenerators("John", "Smith");

        when(trainingTypeDao.findByName("Boxing")).thenReturn(boxing());
        trainerService.save(trainer);

        String username = trainer.getUser().getUsername();
        when(trainerDao.findByUserName(username)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.findByUsername(username);

        assertThat(result.getUser().getUsername()).isEqualTo(username);
        assertThat(result.getUser().getFirstName()).isEqualTo("John");
        assertThat(result.getUser().getLastName()).isEqualTo("Smith");
    }

    @Test
    void findByUsernameThrowsWhenNotFound() {
        when(trainerDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findByUsername("unknown"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Trainer not found: unknown");
    }

    private void setupGenerators(String firstName, String lastName) {
        when(usernameGenerator.generate(firstName, lastName))
                .thenReturn(firstName + "." + lastName);
        passwordGenerator.when(PasswordGenerator::generate).thenReturn("pass123ABC");
    }

    private TrainingType boxing() {
        return TrainingType.builder().trainingTypeName("Boxing").build();
    }

    private Trainer buildTrainer(String firstName, String lastName, String specialization) {
        return Trainer.builder()
                .user(User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .isActive(true)
                        .build())
                .specialization(TrainingType.builder()
                        .trainingTypeName(specialization)
                        .build())
                .build();
    }
}