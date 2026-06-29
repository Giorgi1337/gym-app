package com.gym.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for updating a trainer profile")
public record TrainerUpdateRequest(

        @Schema(description = "First name", example = "Levan")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name", example = "Tsereteli")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(
                description = "Specialization (training type reference). Read-only: any value submitted here is ignored — specialization cannot be changed after registration.",
                example = "Yoga"
        )
        String specialization,

        @Schema(description = "Whether the trainer account is active", example = "true")
        @NotNull(message = "isActive is required")
        Boolean isActive

) {
}