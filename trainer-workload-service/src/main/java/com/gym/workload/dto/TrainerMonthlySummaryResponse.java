package com.gym.workload.dto;

import java.util.List;

public record TrainerMonthlySummaryResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        List<YearSummary> years
) {
    public record YearSummary(int year, List<MonthSummary> months) {}
    public record MonthSummary(int month, int trainingSummaryDuration) {}
}