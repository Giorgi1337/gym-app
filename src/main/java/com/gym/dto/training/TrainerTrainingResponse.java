package com.gym.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Training entry as seen from a trainer's training list")
public record TrainerTrainingResponse(

        @Schema(description = "Training name", example = "Morning Yoga")
        String trainingName,

        @Schema(description = "Training date", example = "2026-06-15")
        LocalDate trainingDate,

        @Schema(description = "Training type (training type name)", example = "Yoga")
        String trainingType,

        @Schema(description = "Training duration in minutes", example = "60")
        Integer trainingDuration,

        @Schema(description = "Trainee's full name", example = "Nika Beridze")
        String traineeName

) {
}