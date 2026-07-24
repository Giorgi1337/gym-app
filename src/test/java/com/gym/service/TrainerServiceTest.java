package com.gym.service;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.repository.TrainerRepository;
import com.gym.repository.TrainingTypeRepository;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TrainerServiceTest {

    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private UsernameGenerator usernameGenerator;
    private GymMetrics gymMetrics;
    private PasswordEncoder passwordEncoder;
    private TrainerService trainerService;

    private MockedStatic<PasswordGenerator> passwordGeneratorMock;

    @BeforeEach
    void setUp() {
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        gymMetrics = mock(GymMetrics.class);
        passwordEncoder = mock(PasswordEncoder.class);

        trainerService = new TrainerService(
                trainerRepository, trainingTypeRepository, usernameGenerator, gymMetrics, passwordEncoder);

        passwordGeneratorMock = mockStatic(PasswordGenerator.class);
    }

    @AfterEach
    void tearDown() {
        passwordGeneratorMock.close();
    }

    @Test
    void saveShouldNormalizeNamesResolveSpecializationAndSaveTrainer() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("john");
        request.setLastName("smith");
        request.setSpecialization("  Boxing  ");

        TrainingType dbSpecialization = new TrainingType();
        dbSpecialization.setTrainingTypeName("Boxing");

        passwordGeneratorMock.when(PasswordGenerator::generate).thenReturn("pass123ABC");
        when(passwordEncoder.encode("pass123ABC")).thenReturn("encodedPassword");
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(trainingTypeRepository.findByTrainingTypeNameEqualsIgnoreCase("Boxing"))
                .thenReturn(Optional.of(dbSpecialization));

        RegistrationResponse response = trainerService.save(request);

        ArgumentCaptor<Trainer> savedCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(savedCaptor.capture());
        Trainer saved = savedCaptor.getValue();

        assertThat(saved.getUser().getFirstName()).isEqualTo("John");
        assertThat(saved.getUser().getLastName()).isEqualTo("Smith");
        assertThat(saved.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(saved.getUser().getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getSpecialization()).isEqualTo(dbSpecialization);

        assertThat(response.getUsername()).isEqualTo("John.Smith");
        assertThat(response.getPassword()).isEqualTo("pass123ABC");

        verify(gymMetrics).incrementTrainerRegistration();
    }

    @Test
    void saveWhenSpecializationNotFoundThrowsResourceNotFoundException() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Smith");
        request.setSpecialization("UnknownType");

        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(trainingTypeRepository.findByTrainingTypeNameEqualsIgnoreCase("UnknownType"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.save(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialization not found: UnknownType");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void findByUsernameWithExistingUsernameReturnsProfileResponse() {
        Trainer trainer = buildTrainer("John.Smith", "John", "Smith", "Yoga", true);

        when(trainerRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainer));

        TrainerProfileResponse response = trainerService.findByUsername("John.Smith");

        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Smith");
        assertThat(response.getSpecialization()).isEqualTo("Yoga");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    void findByUsernameWithUnknownUsernameThrowsResourceNotFoundException() {
        when(trainerRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.findByUsername("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");
    }

    @Test
    void updateWithValidDataUpdatesAndReturnsResponse() {
        Trainer trainer = buildTrainer("John.Smith", "John", "Smith", "Yoga", true);

        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setFirstName("Johnny");
        request.setLastName("Smithers");
        request.setIsActive(true);

        when(trainerRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainer));

        TrainerUpdateResponse response = trainerService.update("John.Smith", request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("Johnny");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smithers");
        assertThat(response.getFirstName()).isEqualTo("Johnny");
        assertThat(response.getLastName()).isEqualTo("Smithers");
        assertThat(response.getSpecialization()).isEqualTo("Yoga");

        verify(trainerRepository).save(trainer);
    }

    @Test
    void updateWithUnknownUsernameThrowsResourceNotFoundException() {
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        when(trainerRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.update("unknown", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void setActiveUpdatesStatus() {
        Trainer trainer = buildTrainer("John.Smith", "John", "Smith", "Yoga", false);

        when(trainerRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainer));

        trainerService.setActive("John.Smith", true);

        assertThat(trainer.getUser().getIsActive()).isTrue();
        verify(trainerRepository).save(trainer);
    }

    @Test
    void setActiveWithUnknownUsernameThrowsResourceNotFoundException() {
        when(trainerRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.setActive("unknown", true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");

        verify(trainerRepository, never()).save(any());
    }

    private Trainer buildTrainer(String username, String firstName, String lastName,
                                 String specializationName, boolean isActive) {
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIsActive(isActive);

        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName(specializationName);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);
        trainer.setTrainees(new HashSet<>());
        return trainer;
    }
}