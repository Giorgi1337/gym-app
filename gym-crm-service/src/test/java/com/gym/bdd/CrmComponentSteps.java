package com.gym.bdd;

import com.gym.dto.AddTrainingRequest;
import com.gym.exception.ResourceNotFoundException;
import com.gym.integration.workload.TrainerWorkloadRequest;
import com.gym.integration.workload.WorkloadGateway;
import com.gym.metrics.GymMetrics;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.TrainingType;
import com.gym.repository.TraineeRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.TrainingRepository;
import com.gym.repository.TrainingTypeRepository;
import com.gym.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CrmComponentSteps {
    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private WorkloadGateway workloadGateway;
    private TrainingService service;
    private Trainer trainer;
    private Trainee trainee;
    private Training training;
    private int requestedDuration;
    private Throwable failure;

    @Before
    public void setUp() {
        trainingRepository = mock(TrainingRepository.class);
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        workloadGateway = mock(WorkloadGateway.class);
        service = new TrainingService(trainingRepository, traineeRepository, trainerRepository,
                mock(TrainingTypeRepository.class), mock(GymMetrics.class), workloadGateway);
        trainer = new Trainer();
        TrainingType specialization = new TrainingType();
        specialization.setTrainingTypeName("Strength");
        trainer.setSpecialization(specialization);
        trainee = new Trainee();
        training = null;
    }

    @Given("trainer {string} and trainee {string} exist")
    public void usersExist(String trainerUsername, String traineeUsername) {
        when(trainerRepository.findByUser_Username(trainerUsername)).thenReturn(Optional.of(trainer));
        when(traineeRepository.findByUser_Username(traineeUsername)).thenReturn(Optional.of(trainee));
    }

    @Given("trainer {string} does not exist")
    public void trainerDoesNotExist(String username) {
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.empty());
    }

    @When("a {int} minute training is added for them")
    public void addForKnownUsers(int duration) {
        add("Nika.Beridze", "Giorgi.Kapanadze", duration);
    }

    @When("a {int} minute training is added for trainer {string}")
    public void addForTrainer(int duration, String trainerUsername) {
        add(trainerUsername, "Giorgi.Kapanadze", duration);
    }

    private void add(String trainerUsername, String traineeUsername, int duration) {
        requestedDuration = duration;
        var request = new AddTrainingRequest();
        request.setTrainingName("Strength session");
        request.setTrainingDate(OffsetDateTime.parse("2026-08-20T10:00:00Z"));
        request.setTrainingDuration(duration);
        try {
            service.addTraining(trainerUsername, traineeUsername, request);
        } catch (Throwable exception) {
            failure = exception;
        }
    }

    @Then("the training is persisted")
    public void trainingIsPersisted() {
        assertThat(failure).isNull();
        var trainingCaptor = org.mockito.ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(trainingCaptor.capture());
        training = trainingCaptor.getValue();
        assertThat(training.getTrainingName()).isEqualTo("Strength session");
        assertThat(training.getTrainingDuration()).isEqualTo(requestedDuration);
    }

    @Then("an ADD workload event is published")
    public void workloadEventIsPublished() {
        verify(workloadGateway).send(training, TrainerWorkloadRequest.ActionType.ADD);
    }

    @Then("the CRM reports that the trainer was not found")
    public void trainerNotFound() {
        assertThat(failure).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: missing");
    }

    @Then("no training or workload event is produced")
    public void nothingProduced() {
        verifyNoInteractions(trainingRepository, workloadGateway);
    }
}
