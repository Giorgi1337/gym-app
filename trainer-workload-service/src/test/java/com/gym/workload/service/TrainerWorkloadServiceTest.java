package com.gym.workload.service;

import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.exception.TrainerNotFoundException;
import com.gym.workload.model.TrainerWorkload;
import com.gym.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TrainerWorkloadServiceTest {
    private TrainerWorkloadRepository repository;
    private TrainerWorkloadService service;

    @BeforeEach
    void setUp() {
        repository = mock(TrainerWorkloadRepository.class);
        service = new TrainerWorkloadService(repository);
    }

    @Test
    void createsDocumentAndAggregatesDurationsByYearAndMonth() {
        when(repository.findByUsername("Nika.Beridze1"))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(savedWorkload()));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.applyWorkload(request(LocalDate.of(2026, 8, 5), 60, TrainerWorkloadRequest.ActionType.ADD));
        TrainerWorkload saved = savedWorkload();
        service.applyWorkload(request(LocalDate.of(2026, 8, 8), 45, TrainerWorkloadRequest.ActionType.ADD));

        verify(repository, times(2)).save(any(TrainerWorkload.class));
        assertThat(saved.getYears().getFirst().getMonths().getFirst().getTrainingSummaryDuration()).isEqualTo(105);
    }

    @Test
    void deletionSubtractsDurationAndNeverCreatesNegativeTotal() {
        TrainerWorkload workload = workloadWithDuration(60);
        when(repository.findByUsername("Nika.Beridze1")).thenReturn(Optional.of(workload));

        service.applyWorkload(request(LocalDate.of(2026, 8, 5), 90, TrainerWorkloadRequest.ActionType.DELETE));

        assertThat(workload.getYears()).isEmpty();
        verify(repository).save(workload);
    }

    @Test
    void returnsSortedPersistedSummary() {
        TrainerWorkload workload = workloadWithDuration(60);
        workload.getYears().add(new TrainerWorkload.YearSummary(2025, new java.util.ArrayList<>()));
        when(repository.findByUsername("Nika.Beridze1")).thenReturn(Optional.of(workload));

        var result = service.getSummary("Nika.Beridze1");

        assertThat(result.years()).extracting(year -> year.year()).containsExactly(2025, 2026);
    }

    @Test
    void unknownTrainerHasNoSummary() {
        when(repository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSummary("unknown"))
                .isInstanceOf(TrainerNotFoundException.class);
    }

    private TrainerWorkload savedWorkload() {
        return org.mockito.Mockito.mockingDetails(repository).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("save"))
                .reduce((first, second) -> second)
                .map(invocation -> (TrainerWorkload) invocation.getArgument(0))
                .orElseThrow();
    }

    private TrainerWorkload workloadWithDuration(int duration) {
        var workload = new TrainerWorkload("Nika.Beridze1", "Nika", "Beridze", true);
        var year = new TrainerWorkload.YearSummary(2026, new ArrayList<>());
        year.getMonths().add(new TrainerWorkload.MonthSummary(8, duration));
        workload.getYears().add(year);
        return workload;
    }

    private TrainerWorkloadRequest request(LocalDate date, int duration, TrainerWorkloadRequest.ActionType action) {
        return new TrainerWorkloadRequest("Nika.Beridze1", "Nika", "Beridze", true, date, duration, action);
    }
}
