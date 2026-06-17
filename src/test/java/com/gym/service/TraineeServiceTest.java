package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.model.Trainee;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {

    private TraineeDao traineeDao;
    private TraineeService traineeService;
    private UsernameGenerator usernameGenerator;
    private MockedStatic<PasswordGenerator> passwordGenerator;
    private Validator validator;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        traineeDao = mock(TraineeDao.class);
        usernameGenerator = mock(UsernameGenerator.class);
        traineeService = new TraineeService(traineeDao, usernameGenerator, validator);
        passwordGenerator = mockStatic(PasswordGenerator.class);
    }

    @AfterEach
    void tearDown() {
        passwordGenerator.close();
    }

    @Test
    void saveSetsUsernameAndPassword() {
        Trainee trainee = buildTrainee("John", "Smith");
        setupGenerators("John", "Smith");

        traineeService.save(trainee);

        assertThat(trainee.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(trainee.getUser().getPassword()).isEqualTo("pass123ABC");
    }

    @Test
    void saveNormalizesNames() {
        Trainee trainee = buildTrainee("john", "smith");
        setupGenerators("John", "Smith");

        traineeService.save(trainee);

        assertThat(trainee.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainee.getUser().getLastName()).isEqualTo("Smith");
    }

    @Test
    void saveCallsUsernameGeneratorWithNormalizedNames() {
        Trainee trainee = buildTrainee("john", "smith");
        setupGenerators("John", "Smith");

        traineeService.save(trainee);

        verify(usernameGenerator).generate("John", "Smith");
    }

    @Test
    void saveThrowsWhenUserIsNull() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.setUser(null);

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("User is required");
    }

    @Test
    void saveThrowsWhenFirstNameIsBlank() {
        Trainee trainee = buildTrainee("", "Smith");

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("First name is required");
    }

    @Test
    void saveThrowsWhenLastNameIsBlank() {
        Trainee trainee = buildTrainee("John", "");

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Last name is required");
    }

    @Test
    void saveThrowsWhenFirstNameHasInvalidCharacters() {
        Trainee trainee = buildTrainee("J0hn!", "Smith");

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("First name contains invalid characters");
    }

    @Test
    void saveThrowsWhenLastNameHasInvalidCharacters() {
        Trainee trainee = buildTrainee("John", "Sm1th@");

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Last name contains invalid characters");
    }

    @Test
    void saveThrowsWhenIsActiveIsNull() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.getUser().setIsActive(null);

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Active status is required");
    }

    @Test
    void saveThrowsWhenDateOfBirthIsInFuture() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.setDateOfBirth(LocalDate.now().plusYears(1));

        assertThatThrownBy(() -> traineeService.save(trainee))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Date of birth must be in the past");
    }

    @Test
    void saveWithNullDateOfBirthSucceeds() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.setDateOfBirth(null);
        setupGenerators("John", "Smith");

        assertThatNoException().isThrownBy(() -> traineeService.save(trainee));
    }

    @Test
    void saveWithNullAddressSucceeds() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.setAddress(null);
        setupGenerators("John", "Smith");

        assertThatNoException().isThrownBy(() -> traineeService.save(trainee));
    }

    @Test
    void findByUsernameReturnsTrainee() {
        Trainee trainee = buildTrainee("John", "Smith");
        setupGenerators("John", "Smith");
        traineeService.save(trainee);

        String username = trainee.getUser().getUsername();
        when(traineeDao.findByUserName(username)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.findByUsername(username);

        assertThat(result.getUser().getUsername()).isEqualTo(username);
        assertThat(result.getUser().getFirstName()).isEqualTo("John");
        assertThat(result.getUser().getLastName()).isEqualTo("Smith");
    }

    @Test
    void findByUsernameThrowsWhenNotFound() {
        when(traineeDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findByUsername("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    private void setupGenerators(String firstName, String lastName) {
        when(usernameGenerator.generate(firstName, lastName))
                .thenReturn(firstName + "." + lastName);
        passwordGenerator.when(PasswordGenerator::generate).thenReturn("pass123ABC");
    }

    private Trainee buildTrainee(String firstName, String lastName) {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .isActive(true)
                        .build())
                .dateOfBirth(LocalDate.of(1999, 5, 6))
                .build();
    }
}