package com.gym.workload.service;

import com.gym.workload.dto.TrainerMonthlySummaryResponse;
import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.exception.TrainerNotFoundException;
import com.gym.workload.model.TrainerWorkload;
import com.gym.workload.repository.InMemoryTrainerWorkloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TrainerWorkloadService {

    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadService.class);

    private final InMemoryTrainerWorkloadRepository repository;

    public TrainerWorkloadService(InMemoryTrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    public void applyWorkload(TrainerWorkloadRequest request) {
        TrainerWorkload workload = repository.findOrCreate(
                request.trainerUsername(),
                request.trainerFirstName(),
                request.trainerLastName(),
                request.isActive());

        workload.updateProfile(request.trainerFirstName(), request.trainerLastName(), request.isActive());

        int minutes = request.actionType() == TrainerWorkloadRequest.ActionType.DELETE
                ? -request.trainingDuration()
                : request.trainingDuration();

        workload.applyDelta(
                request.trainingDate().getYear(),
                request.trainingDate().getMonthValue(),
                minutes);

        log.info("Applied {} of {} min for trainer={} on {}",
                request.actionType(), request.trainingDuration(),
                request.trainerUsername(), request.trainingDate());
    }

    public TrainerMonthlySummaryResponse getSummary(String username) {
        TrainerWorkload workload = repository.find(username);
        if (workload == null) {
            throw new TrainerNotFoundException("No workload data for trainer: " + username);
        }

        List<TrainerMonthlySummaryResponse.YearSummary> years = new ArrayList<>();
        workload.getYearlyData().forEach((year, monthsMap) -> {
            List<TrainerMonthlySummaryResponse.MonthSummary> months = new ArrayList<>();
            monthsMap.forEach((month, total) ->
                    months.add(new TrainerMonthlySummaryResponse.MonthSummary(month, total.get())));
            months.sort(Comparator.comparingInt(TrainerMonthlySummaryResponse.MonthSummary::month));
            years.add(new TrainerMonthlySummaryResponse.YearSummary(year, months));

        });
        years.sort(Comparator.comparingInt(TrainerMonthlySummaryResponse.YearSummary::year));

        return new TrainerMonthlySummaryResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.isActive(),
                years);
    }
}
