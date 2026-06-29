package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.dto.training.AddTrainingRequest;
import com.gym.dto.training.TraineeTrainingResponse;
import com.gym.dto.training.TrainerTrainingResponse;
import com.gym.dto.training.TrainingTypeResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainingMapper;
import com.gym.mapper.TrainingTypeMapper;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TrainingServiceTest {

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private TrainingService trainingService;

    private MockedStatic<TrainingMapper> trainingMapperMock;
    private MockedStatic<TrainingTypeMapper> trainingTypeMapperMock;

    @BeforeEach
    void setup() {
        trainingDao = mock(TrainingDao.class);
        traineeDao = mock(TraineeDao.class);
        trainerDao = mock(TrainerDao.class);
        trainingTypeDao = mock(TrainingTypeDao.class);

        trainingService = new TrainingService(trainingDao, traineeDao, trainerDao, trainingTypeDao);

        trainingMapperMock = mockStatic(TrainingMapper.class);
        trainingTypeMapperMock = mockStatic(TrainingTypeMapper.class);
    }

    @AfterEach
    void tearDown() {
        trainingMapperMock.close();
        trainingTypeMapperMock.close();
    }

    @Test
    void getTraineeTrainingsReturnsMappedListWhenTraineeExists() {
        String username = "john.doe";
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now();
        String trainerName = "trainer";
        String type = "Yoga";

        Trainee trainee = new Trainee();
        Training training = new Training();
        TraineeTrainingResponse expectedResponse = mock(TraineeTrainingResponse.class);

        when(traineeDao.getProfile(username)).thenReturn(Optional.of(trainee));
        when(trainingDao.findByTraineeUsername(username, from, to, trainerName, type)).thenReturn(List.of(training));
        trainingMapperMock.when(() -> TrainingMapper.toTraineeView(training)).thenReturn(expectedResponse);

        List<TraineeTrainingResponse> results = trainingService.getTraineeTrainings(username, from, to, trainerName, type);

        assertThat(results).containsExactly(expectedResponse);
    }

    @Test
    void getTraineeTrainingsThrowsResourceNotFoundExceptionWhenTraineeMissing() {
        String username = "unknown";
        when(traineeDao.getProfile(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTraineeTrainings(username, null, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: " + username);

        verifyNoInteractions(trainingDao);
    }

    @Test
    void getTrainerTrainingsReturnsMappedListWhenTrainerExists() {
        String username = "nika.doe";
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now();
        String traineeName = "trainee";

        Trainer trainer = new Trainer();
        Training training = new Training();
        TrainerTrainingResponse expectedResponse = mock(TrainerTrainingResponse.class);

        when(trainerDao.findByUserName(username)).thenReturn(Optional.of(trainer));
        when(trainingDao.findByTrainerUsername(username, from, to, traineeName)).thenReturn(List.of(training));
        trainingMapperMock.when(() -> TrainingMapper.toTrainerView(training)).thenReturn(expectedResponse);

        List<TrainerTrainingResponse> results = trainingService.getTrainerTrainings(username, from, to, traineeName);

        assertThat(results).containsExactly(expectedResponse);
    }

    @Test
    void getTrainerTrainingsThrowsResourceNotFoundExceptionWhenTrainerMissing() {
        String username = "unknown";
        when(trainerDao.findByUserName(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTrainerTrainings(username, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: " + username);

        verifyNoInteractions(trainingDao);
    }

    @Test
    void addTrainingSucceedsWhenBothEntitiesExist() {
        String trainerUsername = "nika.doe";
        String traineeUsername = "john.doe";
        AddTrainingRequest request = mock(AddTrainingRequest.class);

        Trainer trainer = new Trainer();
        Trainee trainee = new Trainee();
        Training training = new Training();

        when(trainerDao.findByUserName(trainerUsername)).thenReturn(Optional.of(trainer));
        when(traineeDao.getProfile(traineeUsername)).thenReturn(Optional.of(trainee));
        trainingMapperMock.when(() -> TrainingMapper.toEntity(request, trainee, trainer)).thenReturn(training);

        trainingService.addTraining(trainerUsername, traineeUsername, request);

        verify(trainingDao).save(training);
    }

    @Test
    void addTrainingThrowsResourceNotFoundExceptionWhenTrainerNotFound() {
        String trainerUsername = "unknown";
        String traineeUsername = "john.doe";
        AddTrainingRequest request = mock(AddTrainingRequest.class);

        when(trainerDao.findByUserName(trainerUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(trainerUsername, traineeUsername, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: " + trainerUsername);

        verifyNoInteractions(traineeDao, trainingDao);
    }

    @Test
    void addTrainingThrowsResourceNotFoundExceptionWhenTraineeNotFound() {
        String trainerUsername = "nika.doe";
        String traineeUsername = "unknown";
        AddTrainingRequest request = mock(AddTrainingRequest.class);
        Trainer trainer = new Trainer();

        when(trainerDao.findByUserName(trainerUsername)).thenReturn(Optional.of(trainer));
        when(traineeDao.getProfile(traineeUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(trainerUsername, traineeUsername, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: " + traineeUsername);

        verify(trainingDao, never()).save(any());
    }

    @Test
    void getTrainingTypesReturnsMappedResponseList() {
        TrainingType type = new TrainingType();
        TrainingTypeResponse expectedResponse = mock(TrainingTypeResponse.class);

        when(trainingTypeDao.findAll()).thenReturn(List.of(type));
        trainingTypeMapperMock.when(() -> TrainingTypeMapper.toResponse(type)).thenReturn(expectedResponse);

        List<TrainingTypeResponse> results = trainingService.getTrainingTypes();

        assertThat(results).containsExactly(expectedResponse);
    }
}