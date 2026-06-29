package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.dao.TrainerDao;
import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainee.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TraineeMapper;
import com.gym.mapper.TrainerMapper;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TraineeServiceTest {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private TraineeService traineeService;

    private MockedStatic<PasswordGenerator> passwordGeneratorMock;
    private MockedStatic<TraineeMapper> traineeMapperMock;
    private MockedStatic<TrainerMapper> trainerMapperMock;

    @BeforeEach
    void setup() {
        traineeDao = mock(TraineeDao.class);
        trainerDao = mock(TrainerDao.class);
        usernameGenerator = mock(UsernameGenerator.class);

        traineeService = new TraineeService(traineeDao, trainerDao, usernameGenerator);

        passwordGeneratorMock = mockStatic(PasswordGenerator.class);
        traineeMapperMock = mockStatic(TraineeMapper.class);
        trainerMapperMock = mockStatic(TrainerMapper.class);
    }

    @AfterEach
    void tearDown() {
        passwordGeneratorMock.close();
        traineeMapperMock.close();
        trainerMapperMock.close();
    }

    @Test
    void registerShouldNormalizeNamesAndSaveTrainee() {
        TraineeRegistrationRequest request = mock(TraineeRegistrationRequest.class);
        Trainee trainee = buildTrainee("john", "smith");
        RegistrationResponse expectedResponse = mock(RegistrationResponse.class);

        traineeMapperMock.when(() -> TraineeMapper.toEntity(request)).thenReturn(trainee);
        passwordGeneratorMock.when(PasswordGenerator::generate).thenReturn("pass123ABC");
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        traineeMapperMock.when(() -> TraineeMapper.toRegistrationResponse(trainee)).thenReturn(expectedResponse);

        RegistrationResponse actualResponse = traineeService.register(request);

        assertThat(trainee.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainee.getUser().getLastName()).isEqualTo("Smith");
        assertThat(trainee.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(trainee.getUser().getPassword()).isEqualTo("pass123ABC");

        verify(traineeDao).save(trainee);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getProfileWithExistingUsernameReturnsProfileResponse() {
        Trainee trainee = buildTrainee("John", "Smith");
        TraineeProfileResponse expectedResponse = mock(TraineeProfileResponse.class);

        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(trainee));
        traineeMapperMock.when(() -> TraineeMapper.toProfileResponse(trainee)).thenReturn(expectedResponse);

        TraineeProfileResponse actualResponse = traineeService.getProfile("John.Smith");

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getProfileWithUnknownUsernameThrowsResourceNotFoundException() {
        when(traineeDao.getProfile("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    @Test
    void updateWithValidDataUpdatesAndReturnsResponse() {
        TraineeUpdateRequest request = mock(TraineeUpdateRequest.class);
        Trainee trainee = buildTrainee("John", "Smith");
        TraineeUpdateResponse expectedResponse = mock(TraineeUpdateResponse.class);

        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(trainee));
        traineeMapperMock.when(() -> TraineeMapper.toUpdateResponse(trainee)).thenReturn(expectedResponse);

        TraineeUpdateResponse actualResponse = traineeService.update("John.Smith", request);

        traineeMapperMock.verify(() -> TraineeMapper.applyUpdate(trainee, request));
        verify(traineeDao).update(trainee);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void deleteWithExistingUsernameExecutesSuccessfully() {
        Trainee trainee = buildTrainee("John", "Smith");
        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(trainee));

        traineeService.delete("John.Smith");

        verify(traineeDao).delete(trainee);
    }

    @Test
    void getUnassignedTrainersFiltersOnlyActiveTrainers() {
        Trainer activeTrainer = buildTrainer("Active.Trainer", true);
        Trainer inactiveTrainer = buildTrainer("Inactive.Trainer", false);

        TraineeProfileResponse.TrainerSummary summary = mock(TraineeProfileResponse.TrainerSummary.class);

        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(buildTrainee("John", "Smith")));
        when(traineeDao.findUnassignedTrainers("John.Smith")).thenReturn(List.of(activeTrainer, inactiveTrainer));
        trainerMapperMock.when(() -> TrainerMapper.toSummary(activeTrainer)).thenReturn(summary);

        List<TraineeProfileResponse.TrainerSummary> result = traineeService.getUnassignedTrainers("John.Smith");

        assertThat(result).hasSize(1).containsExactly(summary);
    }

    @Test
    void updateTrainersWithValidTrainersUpdatesTraineeTrainersList() {
        UpdateTraineeTrainersRequest request = mock(UpdateTraineeTrainersRequest.class);
        UpdateTraineeTrainersRequest.TrainerRef ref = mock(UpdateTraineeTrainersRequest.TrainerRef.class);

        Trainee trainee = buildTrainee("John", "Smith");
        Trainer trainer = buildTrainer("Trainer.One", true);
        TraineeProfileResponse.TrainerSummary summary = mock(TraineeProfileResponse.TrainerSummary.class);

        when(ref.trainerUsername()).thenReturn("Trainer.One");
        when(request.trainers()).thenReturn(List.of(ref));
        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsernames(Set.of("Trainer.One"))).thenReturn(List.of(trainer));
        trainerMapperMock.when(() -> TrainerMapper.toSummary(trainer)).thenReturn(summary);

        List<TraineeProfileResponse.TrainerSummary> result = traineeService.updateTrainers("John.Smith", request);

        assertThat(trainee.getTrainers()).containsExactly(trainer);
        verify(traineeDao).update(trainee);
        assertThat(result).containsExactly(summary);
    }

    @Test
    void updateTrainersWithMissingTrainersThrowsResourceNotFoundException() {
        UpdateTraineeTrainersRequest request = mock(UpdateTraineeTrainersRequest.class);
        UpdateTraineeTrainersRequest.TrainerRef ref = mock(UpdateTraineeTrainersRequest.TrainerRef.class);

        when(ref.trainerUsername()).thenReturn("Missing.Trainer");
        when(request.trainers()).thenReturn(List.of(ref));
        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(buildTrainee("John", "Smith")));
        when(trainerDao.findByUsernames(Set.of("Missing.Trainer"))).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> traineeService.updateTrainers("John.Smith", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer(s) not found");
    }

    @Test
    void setActiveUpdatesStatusAndReturnsResponse() {
        Trainee trainee = buildTrainee("John", "Smith");
        trainee.getUser().setIsActive(false);
        TraineeUpdateResponse expectedResponse = mock(TraineeUpdateResponse.class);

        when(traineeDao.getProfile("John.Smith")).thenReturn(Optional.of(trainee));
        traineeMapperMock.when(() -> TraineeMapper.toUpdateResponse(trainee)).thenReturn(expectedResponse);

        TraineeUpdateResponse actualResponse = traineeService.setActive("John.Smith", true);

        assertThat(trainee.getUser().getIsActive()).isTrue();
        verify(traineeDao).update(trainee);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private Trainee buildTrainee(String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setTrainers(new HashSet<>());
        return trainee;
    }

    private Trainer buildTrainer(String username, boolean isActive) {
        User user = new User();
        user.setUsername(username);
        user.setIsActive(isActive);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        return trainer;
    }
}