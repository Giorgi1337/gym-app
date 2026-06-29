package com.gym.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Payload for updating a trainee profile")
public record TraineeUpdateRequest(

        @Schema(description = "First name", example = "Nika")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name", example = "Beridze")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Date of birth", example = "1998-05-12")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Schema(description = "Home address", example = "Tbilisi, Georgia")
        String address,

        @Schema(description = "Whether the trainee account is active", example = "true")
        @NotNull(message = "isActive is required")
        Boolean isActive

) {
}