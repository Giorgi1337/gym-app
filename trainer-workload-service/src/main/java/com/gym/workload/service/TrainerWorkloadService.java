package com.gym.workload.service;

import com.gym.workload.dto.TrainerMonthlySummaryResponse;
import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.exception.TrainerNotFoundException;
import com.gym.workload.model.TrainerWorkload;
import com.gym.workload.repository.TrainerWorkloadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;

@Service
public class TrainerWorkloadService {
    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadService.class);
    private final TrainerWorkloadRepository repository;

    public TrainerWorkloadService(TrainerWorkloadRepository repository) {
        this.repository = repository;
    }

    public void applyWorkload(TrainerWorkloadRequest request) {
        log.debug("Looking up trainer workload by username = {}", request.trainerUsername());
        TrainerWorkload workload = repository.findByUsername(request.trainerUsername())
                .orElseGet(() -> {
                    log.debug("Creating workload document for username = {}", request.trainerUsername());
                    return new TrainerWorkload(request.trainerUsername(), request.trainerFirstName(),
                            request.trainerLastName(), request.isActive());
                });

        workload.setFirstName(request.trainerFirstName());
        workload.setLastName(request.trainerLastName());
        workload.setActive(request.isActive());

        int yearValue = request.trainingDate().getYear();
        int monthValue = request.trainingDate().getMonthValue();
        TrainerWorkload.YearSummary year = workload.getYears().stream()
                .filter(item -> item.getYear() == yearValue)
                .findFirst()
                .orElseGet(() -> {
                    var item = new TrainerWorkload.YearSummary(yearValue, new ArrayList<>());
                    workload.getYears().add(item);
                    return item;
                });
        TrainerWorkload.MonthSummary month = year.getMonths().stream()
                .filter(item -> item.getMonth() == monthValue)
                .findFirst()
                .orElseGet(() -> {
                    var item = new TrainerWorkload.MonthSummary(monthValue, 0);
                    year.getMonths().add(item);
                    return item;
                });

        int delta = request.actionType() == TrainerWorkloadRequest.ActionType.DELETE
                ? -request.trainingDuration() : request.trainingDuration();
        month.setTrainingSummaryDuration(Math.max(0, month.getTrainingSummaryDuration() + delta));
        if (month.getTrainingSummaryDuration() == 0) {
            year.getMonths().remove(month);
            if (year.getMonths().isEmpty()) workload.getYears().remove(year);
        }

        repository.save(workload);
        log.debug("Saved workload operation = {} username = {} year = {} month = {} durationDelta = {}",
                request.actionType(), request.trainerUsername(), yearValue, monthValue, delta);
    }

    public TrainerMonthlySummaryResponse getSummary(String username) {
        log.debug("Reading workload summary by username = {}", username);
        TrainerWorkload workload = repository.findByUsername(username)
                .orElseThrow(() -> new TrainerNotFoundException("No workload data for trainer: " + username));

        var years = workload.getYears().stream()
                .sorted(Comparator.comparingInt(TrainerWorkload.YearSummary::getYear))
                .map(year -> new TrainerMonthlySummaryResponse.YearSummary(year.getYear(), year.getMonths().stream()
                        .sorted(Comparator.comparingInt(TrainerWorkload.MonthSummary::getMonth))
                        .map(month -> new TrainerMonthlySummaryResponse.MonthSummary(
                                month.getMonth(), month.getTrainingSummaryDuration()))
                        .toList()))
                .toList();
        return new TrainerMonthlySummaryResponse(
                workload.getUsername(),
                workload.getFirstName(),
                workload.getLastName(),
                workload.getActive(),
                years
        );
    }
}
