package com.gym.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Trainee profile after update")
public record TraineeUpdateResponse(

        @Schema(description = "Username", example = "Nika.Beridze")
        String username,

        @Schema(description = "First name", example = "Nika")
        String firstName,

        @Schema(description = "Last name", example = "Beridze")
        String lastName,

        @Schema(description = "Date of birth", example = "1998-05-12")
        LocalDate dateOfBirth,

        @Schema(description = "Home address", example = "Tbilisi, Georgia")
        String address,

        @Schema(description = "Whether the trainee account is active", example = "true")
        Boolean isActive,

        @Schema(description = "Trainers assigned to this trainee")
        List<TraineeProfileResponse.TrainerSummary> trainers

) {
}