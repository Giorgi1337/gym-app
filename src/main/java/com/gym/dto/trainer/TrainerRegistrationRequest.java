package com.gym.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for registering a new trainer")
public record TrainerRegistrationRequest(

        @Schema(description = "Trainer's first name", example = "Nika")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Trainer's last name", example = "Beridze")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Trainer's specialization (training type name)", example = "Yoga")
        @NotNull(message = "Specialization is required")
        String specialization

) {
}