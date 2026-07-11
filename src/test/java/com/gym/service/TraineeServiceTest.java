package com.gym.service;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {

    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private UsernameGenerator usernameGenerator;
    private GymMetrics gymMetrics;
    private TraineeService traineeService;

    private MockedStatic<PasswordGenerator> passwordGeneratorMock;

    @BeforeEach
    void setup() {
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        gymMetrics = mock(GymMetrics.class);

        traineeService = new TraineeService(traineeRepository, trainerRepository, usernameGenerator, gymMetrics);

        passwordGeneratorMock = mockStatic(PasswordGenerator.class);
    }

    @AfterEach
    void tearDown() {
        passwordGeneratorMock.close();
    }

    @Test
    void saveShouldNormalizeNamesGenerateCredentialsAndSaveTrainee() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("john");
        request.setLastName("smith");
        request.setDateOfBirth(LocalDate.of(1998, 5, 12));
        request.setAddress("12 Rustaveli Ave, Tbilisi");

        passwordGeneratorMock.when(PasswordGenerator::generate).thenReturn("pass123ABC");
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");

        RegistrationResponse response = traineeService.save(request);

        ArgumentCaptor<Trainee> savedCaptor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(savedCaptor.capture());
        Trainee saved = savedCaptor.getValue();

        assertThat(saved.getUser().getFirstName()).isEqualTo("John");
        assertThat(saved.getUser().getLastName()).isEqualTo("Smith");
        assertThat(saved.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(saved.getUser().getPassword()).isEqualTo("pass123ABC");

        assertThat(response.getUsername()).isEqualTo("John.Smith");
        assertThat(response.getPassword()).isEqualTo("pass123ABC");

        verify(gymMetrics).incrementTraineeRegistration();
    }

    @Test
    void findByUsernameWithExistingUsernameReturnsProfileResponse() {
        Trainee trainee = buildTrainee("John", "Smith");

        when(traineeRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainee));

        TraineeProfileResponse response = traineeService.findByUsername("John.Smith");

        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getLastName()).isEqualTo("Smith");
    }

    @Test
    void findByUsernameWithUnknownUsernameThrowsResourceNotFoundException() {
        when(traineeRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.findByUsername("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    @Test
    void updateWithValidDataUpdatesAndReturnsResponse() {
        Trainee trainee = buildTrainee("John", "Smith");

        TraineeUpdateRequest request = new TraineeUpdateRequest();
        request.setFirstName("Johnny");
        request.setLastName("Smithers");
        request.setAddress("New address");
        request.setIsActive(true);

        when(traineeRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainee));

        TraineeUpdateResponse response = traineeService.update("John.Smith", request);

        assertThat(trainee.getUser().getFirstName()).isEqualTo("Johnny");
        assertThat(trainee.getUser().getLastName()).isEqualTo("Smithers");
        assertThat(response.getFirstName()).isEqualTo("Johnny");
        assertThat(response.getLastName()).isEqualTo("Smithers");

        verify(traineeRepository).save(trainee);
    }

    @Test
    void deleteWithExistingUsernameExecutesSuccessfully() {
        Trainee trainee = buildTrainee("John", "Smith");
        when(traineeRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.delete("John.Smith");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void deleteWithUnknownUsernameThrowsResourceNotFoundException() {
        when(traineeRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.delete("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    @Test
    void getUnassignedTrainersFiltersOnlyActiveTrainers() {
        Trainer activeTrainer = buildTrainer("Active.Trainer", true);
        Trainer inactiveTrainer = buildTrainer("Inactive.Trainer", false);

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(buildTrainee("John", "Smith")));
        when(traineeRepository.findUnassignedTrainers("John.Smith"))
                .thenReturn(List.of(activeTrainer, inactiveTrainer));

        List<TrainerSummary> result = traineeService.getUnassignedTrainers("John.Smith");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("Active.Trainer");
    }

    @Test
    void getUnassignedTrainersWithUnknownUsernameThrowsResourceNotFoundException() {
        when(traineeRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getUnassignedTrainers("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    @Test
    void updateTrainersWithValidTrainersUpdatesTraineeTrainersList() {
        Trainee trainee = buildTrainee("John", "Smith");
        Trainer trainer = buildTrainer("Trainer.One", true);

        TrainerRef ref = new TrainerRef();
        ref.setTrainerUsername("Trainer.One");

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTrainers(List.of(ref));

        when(traineeRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(Set.of("Trainer.One"))).thenReturn(List.of(trainer));

        List<TrainerSummary> result = traineeService.updateTrainers("John.Smith", request);

        assertThat(trainee.getTrainers()).containsExactly(trainer);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("Trainer.One");

        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateTrainersWithMissingTrainersThrowsResourceNotFoundException() {
        TrainerRef ref = new TrainerRef();
        ref.setTrainerUsername("Missing.Trainer");

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTrainers(List.of(ref));

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(buildTrainee("John", "Smith")));
        when(trainerRepository.findByUsernames(Set.of("Missing.Trainer"))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> traineeService.updateTrainers("John.Smith", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer(s) not found");
    }

    @Test
    void setActiveUpdatesStatus() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.getUser().setIsActive(false);

        when(traineeRepository.findByUser_Username("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.setActive("John.Smith", true);

        assertThat(trainee.getUser().getIsActive()).isTrue();
        verify(traineeRepository).save(trainee);
    }

    @Test
    void setActiveWithUnknownUsernameThrowsResourceNotFoundException() {
        when(traineeRepository.findByUser_Username("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.setActive("unknown", true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    private Trainee buildTrainee(String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(firstName + "." + lastName);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setTrainers(new HashSet<>());
        return trainee;
    }

    private Trainer buildTrainer(String username, boolean isActive) {
        User user = new User();
        user.setUsername(username);
        user.setIsActive(isActive);

        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);
        return trainer;
    }
}