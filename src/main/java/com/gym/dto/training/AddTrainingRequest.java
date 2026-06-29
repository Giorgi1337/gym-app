package com.gym.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Payload for scheduling a new training between a trainer and a trainee")
public record AddTrainingRequest(

        @Schema(description = "Training name", example = "Morning Yoga")
        @NotBlank(message = "Training name is required")
        String trainingName,

        @Schema(description = "Training date", example = "2026-06-25")
        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @Schema(description = "Training duration in minutes", example = "60")
        @NotNull(message = "Training duration is required")
        @Min(value = 1, message = "Duration must be positive")
        Integer trainingDuration

) {
}