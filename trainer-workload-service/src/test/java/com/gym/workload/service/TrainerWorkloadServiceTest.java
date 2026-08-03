package com.gym.workload.service;

import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.exception.TrainerNotFoundException;
import com.gym.workload.repository.InMemoryTrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainerWorkloadServiceTest {
    private TrainerWorkloadService service;

    @BeforeEach
    void setUp() {
        service = new TrainerWorkloadService(new InMemoryTrainerWorkloadRepository());
    }

    @Test
    void aggregatesDurationsByYearAndMonth() {
        service.applyWorkload(request(LocalDate.of(2026, 8, 5), 60, TrainerWorkloadRequest.ActionType.ADD));
        service.applyWorkload(request(LocalDate.of(2026, 8, 8), 45, TrainerWorkloadRequest.ActionType.ADD));
        service.applyWorkload(request(LocalDate.of(2025, 9, 1), 25, TrainerWorkloadRequest.ActionType.ADD));

        var summary = service.getSummary("Nika.Beridze1");

        assertThat(summary.years()).hasSize(2);
        assertThat(summary.years().get(0).year()).isEqualTo(2025);
        assertThat(summary.years().get(1).months().getFirst().trainingSummaryDuration()).isEqualTo(105);
    }

    @Test
    void deletionSubtractsDurationAndNeverCreatesNegativeTotal() {
        service.applyWorkload(request(LocalDate.of(2026, 8, 5), 60, TrainerWorkloadRequest.ActionType.ADD));
        service.applyWorkload(request(LocalDate.of(2026, 8, 5), 90, TrainerWorkloadRequest.ActionType.DELETE));

        assertThat(service.getSummary("Nika.Beridze1").years()).isEmpty();
    }

    @Test
    void unknownTrainerHasNoSummary() {
        assertThatThrownBy(() -> service.getSummary("unknown"))
                .isInstanceOf(TrainerNotFoundException.class);
    }

    private TrainerWorkloadRequest request(LocalDate date, int duration, TrainerWorkloadRequest.ActionType action) {
        return new TrainerWorkloadRequest(
                "Nika.Beridze1", "Nika", "Beridze", true, date, duration, action);
    }
}
