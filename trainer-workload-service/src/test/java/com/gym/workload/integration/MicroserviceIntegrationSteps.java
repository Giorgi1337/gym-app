package com.gym.workload.integration;

import com.gym.integration.workload.WorkloadGateway;
import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.User;
import com.gym.workload.messaging.TrainerWorkloadListener;
import com.gym.workload.model.TrainerWorkload;
import com.gym.workload.repository.TrainerWorkloadRepository;
import com.gym.workload.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.Validation;
import org.springframework.jms.core.JmsTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MicroserviceIntegrationSteps {
    private final Map<String, TrainerWorkload> store = new HashMap<>();
    private TrainerWorkloadRepository repository;
    private TrainerWorkloadListener listener;
    private JmsTemplate producerTemplate;
    private JmsTemplate consumerTemplate;
    private WorkloadGateway gateway;
    private Training training;
    private com.gym.integration.workload.TrainerWorkloadRequest crmEvent;

    @Before
    public void setUp() {
        store.clear();
        repository = mock(TrainerWorkloadRepository.class);
        when(repository.findByUsername(anyString())).thenAnswer(call -> Optional.ofNullable(store.get(call.getArgument(0))));
        when(repository.save(any())).thenAnswer(call -> {
            TrainerWorkload value = call.getArgument(0);
            store.put(value.getUsername(), value);
            return value;
        });
        producerTemplate = mock(JmsTemplate.class);
        consumerTemplate = mock(JmsTemplate.class);
        gateway = new WorkloadGateway(producerTemplate, "trainer.workload");
        listener = new TrainerWorkloadListener(new TrainerWorkloadService(repository),
                Validation.buildDefaultValidatorFactory().getValidator(), consumerTemplate, "trainer.workload.dlq");
        doAnswer(call -> {
            crmEvent = call.getArgument(1);
            return null;
        }).when(producerTemplate).convertAndSend(eq("trainer.workload"), any(), any(org.springframework.jms.core.MessagePostProcessor.class));
    }

    @Given("a CRM training for trainer {string} on {word} lasting {int} minutes")
    public void crmTraining(String username, String date, int duration) {
        var user = User.builder().username(username).firstName("Nika").lastName("Beridze").isActive(true).build();
        var trainer = Trainer.builder().user(user).build();
        training = Training.builder().trainer(trainer)
                .trainingDate(LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC))
                .trainingDuration(duration).build();
    }

    @Given("a CRM training event with a blank trainer username")
    public void invalidCrmEvent() {
        crmEvent = new com.gym.integration.workload.TrainerWorkloadRequest(
                " ", "Nika", "Beridze", true, LocalDate.of(2026, 8, 20), 45,
                com.gym.integration.workload.TrainerWorkloadRequest.ActionType.ADD);
    }

    @When("the CRM publishes an ADD event and the workload service consumes it")
    public void publishAndConsume() throws Exception {
        gateway.send(training, com.gym.integration.workload.TrainerWorkloadRequest.ActionType.ADD);
        listener.receive(toConsumerEvent(crmEvent), "bdd-integration");
    }

    @When("the workload service consumes the invalid event")
    public void consumeInvalid() throws Exception {
        listener.receive(toConsumerEvent(crmEvent), "bdd-integration");
    }

    private com.gym.workload.dto.TrainerWorkloadRequest toConsumerEvent(
            com.gym.integration.workload.TrainerWorkloadRequest event) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(mapper.writeValueAsBytes(event), com.gym.workload.dto.TrainerWorkloadRequest.class);
    }

    @Then("the integrated workload for August {int} is {int} minutes")
    public void integratedSummary(int year, int duration) {
        TrainerWorkload workload = store.get("Nika.Beridze");
        assertThat(workload).isNotNull();
        assertThat(workload.getYears()).singleElement().satisfies(y -> {
            assertThat(y.getYear()).isEqualTo(year);
            assertThat(y.getMonths()).singleElement()
                    .extracting(TrainerWorkload.MonthSummary::getTrainingSummaryDuration).isEqualTo(duration);
        });
    }

    @Then("the event is sent to the workload dead letter queue")
    public void deadLettered() {
        verify(consumerTemplate).convertAndSend(eq("trainer.workload.dlq"), any(), any(org.springframework.jms.core.MessagePostProcessor.class));
    }

    @Then("no integrated workload is persisted")
    public void notPersisted() {
        assertThat(store).isEmpty();
        verify(repository, never()).save(any());
    }
}
