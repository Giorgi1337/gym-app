package com.gym.service;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.exception.BusinessValidationException;
import com.gym.integration.workload.WorkloadGateway;
import com.gym.mapper.TrainingMapper;
import com.gym.mapper.TrainingTypeMapper;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.TrainingRepository;
import com.gym.repository.TrainingTypeRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TrainingServiceTest {

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private GymMetrics gymMetrics;
    private Timer trainingQueryTimer;
    private WorkloadGateway workloadGateway;
    private TrainingService trainingService;

    private MockedStatic<TrainingMapper> trainingMapperMock;
    private MockedStatic<TrainingTypeMapper> trainingTypeMapperMock;

    @BeforeEach
    void setup() {
        trainingRepository = mock(TrainingRepository.class);
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        gymMetrics = mock(GymMetrics.class);
        trainingQueryTimer = mock(Timer.class);
        workloadGateway = mock(WorkloadGateway.class);

        when(gymMetrics.trainingQueryTimer()).thenReturn(trainingQueryTimer);

        when(trainingQueryTimer.record(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        trainingService = new TrainingService(
                trainingRepository, traineeRepository, trainerRepository, trainingTypeRepository,
                gymMetrics, workloadGateway);

        trainingMapperMock = mockStatic(TrainingMapper.class);
        trainingTypeMapperMock = mockStatic(TrainingTypeMapper.class);
    }

    @AfterEach
    void tearDown() {
        trainingMapperMock.close();
        trainingTypeMapperMock.close();
    }

    @Test
    void getTraineeTrainingsReturnsMappedPageWhenTraineeExists() {
        String username = "john.doe";
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        String trainerName = "trainer";
        String type = "Yoga";
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "trainingDate"));

        Trainee trainee = new Trainee();
        Training training = new Training();
        Page<Training> page = new PageImpl<>(List.of(training), pageable, 1);
        TraineeTrainingPageResponse expectedResponse = mock(TraineeTrainingPageResponse.class);

        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.of(trainee));
        when(trainingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        trainingMapperMock.when(() -> TrainingMapper.toTraineePage(page)).thenReturn(expectedResponse);

        TraineeTrainingPageResponse result =
                trainingService.getTraineeTrainings(username, from, to, trainerName, type, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getTraineeTrainingsAppliesDefaultSortWhenPageableIsUnsorted() {
        String username = "john.doe";
        Pageable unsorted = PageRequest.of(0, 20);

        Trainee trainee = new Trainee();
        Page<Training> page = new PageImpl<>(List.of());
        TraineeTrainingPageResponse expectedResponse = mock(TraineeTrainingPageResponse.class);

        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.of(trainee));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(trainingRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(page);
        trainingMapperMock.when(() -> TrainingMapper.toTraineePage(page)).thenReturn(expectedResponse);

        trainingService.getTraineeTrainings(username, null, null, null, null, unsorted);

        Sort resolvedSort = pageableCaptor.getValue().getSort();
        assertThat(resolvedSort.isSorted()).isTrue();
        assertThat(Objects.requireNonNull(resolvedSort.getOrderFor("trainingDate")).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getTraineeTrainingsThrowsResourceNotFoundExceptionWhenTraineeMissing() {
        String username = "unknown";
        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTraineeTrainings(
                username, null, null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: " + username);

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void getTrainerTrainingsReturnsMappedPageWhenTrainerExists() {
        String username = "nika.doe";
        OffsetDateTime from = OffsetDateTime.now().minusDays(1);
        OffsetDateTime to = OffsetDateTime.now();
        String traineeName = "trainee";
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "trainingDate"));

        Trainer trainer = new Trainer();
        Training training = new Training();
        Page<Training> page = new PageImpl<>(List.of(training), pageable, 1);
        TrainerTrainingPageResponse expectedResponse = mock(TrainerTrainingPageResponse.class);

        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(trainer));
        when(trainingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        trainingMapperMock.when(() -> TrainingMapper.toTrainerPage(page)).thenReturn(expectedResponse);

        TrainerTrainingPageResponse result =
                trainingService.getTrainerTrainings(username, from, to, traineeName, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getTrainerTrainingsThrowsResourceNotFoundExceptionWhenTrainerMissing() {
        String username = "unknown";
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.getTrainerTrainings(
                username, null, null, null, PageRequest.of(0, 20)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: " + username);

        verifyNoInteractions(trainingRepository);
    }

    @Test
    void addTrainingSucceedsWhenBothEntitiesExist() {
        String trainerUsername = "nika.doe";
        String traineeUsername = "john.doe";
        AddTrainingRequest request = mock(AddTrainingRequest.class);
        when(request.getTrainingDate()).thenReturn(OffsetDateTime.now().plusDays(1));

        Trainer trainer = new Trainer();
        Trainee trainee = new Trainee();
        Training training = new Training();

        when(trainerRepository.findByUser_Username(trainerUsername)).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUser_Username(traineeUsername)).thenReturn(Optional.of(trainee));
        trainingMapperMock.when(() -> TrainingMapper.toEntity(request, trainee, trainer)).thenReturn(training);

        trainingService.addTraining(trainerUsername, traineeUsername, request);

        verify(trainingRepository).save(training);
        verify(gymMetrics).incrementTrainingAdded();
    }

//    @Test
//    void addTrainingRejectsPastDate() {
//        AddTrainingRequest request = mock(AddTrainingRequest.class);
//        when(request.getTrainingDate()).thenReturn(OffsetDateTime.now().minusMinutes(1));
//        when(trainerRepository.findByUser_Username("nika.doe")).thenReturn(Optional.of(new Trainer()));
//        when(traineeRepository.findByUser_Username("john.doe")).thenReturn(Optional.of(new Trainee()));
//
//        assertThatThrownBy(() -> trainingService.addTraining("nika.doe", "john.doe", request))
//                .isInstanceOf(BusinessValidationException.class);
//
//        verify(trainingRepository, never()).save(any());
//        verifyNoInteractions(workloadGateway);
//    }
//
//    @Test
//    void addTrainingRejectsDateMoreThanOneMonthAway() {
//        AddTrainingRequest request = mock(AddTrainingRequest.class);
//        when(request.getTrainingDate()).thenReturn(OffsetDateTime.now().plusMonths(1).plusMinutes(1));
//        when(trainerRepository.findByUser_Username("nika.doe")).thenReturn(Optional.of(new Trainer()));
//        when(traineeRepository.findByUser_Username("john.doe")).thenReturn(Optional.of(new Trainee()));
//
//        assertThatThrownBy(() -> trainingService.addTraining("nika.doe", "john.doe", request))
//                .isInstanceOf(BusinessValidationException.class);
//
//        verify(trainingRepository, never()).save(any());
//        verifyNoInteractions(workloadGateway);
//    }

    @Test
    void addTrainingThrowsResourceNotFoundExceptionWhenTrainerNotFound() {
        String trainerUsername = "unknown";
        String traineeUsername = "john.doe";
        AddTrainingRequest request = mock(AddTrainingRequest.class);

        when(trainerRepository.findByUser_Username(trainerUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(trainerUsername, traineeUsername, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: " + trainerUsername);

        verifyNoInteractions(traineeRepository, trainingRepository);
    }

    @Test
    void addTrainingThrowsResourceNotFoundExceptionWhenTraineeNotFound() {
        String trainerUsername = "nika.doe";
        String traineeUsername = "unknown";
        AddTrainingRequest request = mock(AddTrainingRequest.class);
        Trainer trainer = new Trainer();

        when(trainerRepository.findByUser_Username(trainerUsername)).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUser_Username(traineeUsername)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(trainerUsername, traineeUsername, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainee not found: " + traineeUsername);

        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getTrainingTypesReturnsMappedResponseList() {
        TrainingType type = new TrainingType();
        TrainingTypeResponse expectedResponse = mock(TrainingTypeResponse.class);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type));
        trainingTypeMapperMock.when(() -> TrainingTypeMapper.toResponse(type)).thenReturn(expectedResponse);

        List<TrainingTypeResponse> results = trainingService.getTrainingTypes();

        assertThat(results).containsExactly(expectedResponse);
    }
}
