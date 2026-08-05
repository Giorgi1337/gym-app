package com.gym.workload.bdd;

import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.exception.TrainerNotFoundException;
import com.gym.workload.model.TrainerWorkload;
import com.gym.workload.repository.TrainerWorkloadRepository;
import com.gym.workload.service.TrainerWorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WorkloadComponentSteps {
    private final Map<String, TrainerWorkload> store = new HashMap<>();
    private TrainerWorkloadRepository repository;
    private TrainerWorkloadService service;
    private Throwable failure;

    @Before
    public void setUp() {
        store.clear();
        repository = mock(TrainerWorkloadRepository.class);
        when(repository.findByUsername(any())).thenAnswer(call -> Optional.ofNullable(store.get(call.getArgument(0))));
        when(repository.save(any())).thenAnswer(call -> {
            TrainerWorkload value = call.getArgument(0);
            store.put(value.getUsername(), value);
            return value;
        });
        service = new TrainerWorkloadService(repository);
    }

    @Given("no workload exists for trainer {string}")
    public void noWorkload(String username) {
        store.remove(username);
    }

    @When("an ADD workload event of {int} minutes is applied in August {int}")
    public void applyEvent(int duration, int year) {
        service.applyWorkload(new TrainerWorkloadRequest("Nika.Beridze", "Nika", "Beridze", true,
                LocalDate.of(year, 8, 20), duration, TrainerWorkloadRequest.ActionType.ADD));
    }

    @When("the summary for {string} is requested")
    public void requestSummary(String username) {
        try {
            service.getSummary(username);
        } catch (Throwable exception) {
            failure = exception;
        }
    }

    @Then("the trainer's August {int} workload is {int} minutes")
    public void verifyWorkload(int year, int duration) {
        var result = service.getSummary("Nika.Beridze");
        assertThat(result.years()).singleElement().satisfies(summary -> {
            assertThat(summary.year()).isEqualTo(year);
            assertThat(summary.months()).singleElement().satisfies(month -> {
                assertThat(month.month()).isEqualTo(8);
                assertThat(month.trainingSummaryDuration()).isEqualTo(duration);
            });
        });
    }

    @Then("the workload component reports that the trainer was not found")
    public void notFound() {
        assertThat(failure).isInstanceOf(TrainerNotFoundException.class);
    }
}
