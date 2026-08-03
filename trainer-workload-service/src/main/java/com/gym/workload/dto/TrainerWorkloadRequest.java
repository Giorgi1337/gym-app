package com.gym.workload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TrainerWorkloadRequest(

        @NotBlank(message = "Trainer username is required")
        String trainerUsername,

        @NotBlank(message = "Trainer first name is required")
        String trainerFirstName,

        @NotBlank(message = "Trainer last name is required")
        String trainerLastName,

        @NotNull(message = "isActive is required")
        Boolean isActive,

        @NotNull(message = "Training date is required")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        @Min(value = 1, message = "Training duration must be positive")
        Integer trainingDuration,

        @NotNull(message = "Action type is required")
        ActionType actionType
) {
    public enum ActionType {
        ADD,
        DELETE
    }
}
