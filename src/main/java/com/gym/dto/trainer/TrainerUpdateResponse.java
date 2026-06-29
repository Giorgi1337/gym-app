package com.gym.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Trainer profile after update")
public record TrainerUpdateResponse(

        @Schema(description = "Username", example = "Levan.Tsereteli")
        String username,

        @Schema(description = "First name", example = "Levan")
        String firstName,

        @Schema(description = "Last name", example = "Tsereteli")
        String lastName,

        @Schema(description = "Specialization (training type name)", example = "Yoga")
        String specialization,

        @Schema(description = "Whether the trainer account is active", example = "true")
        Boolean isActive,

        @Schema(description = "Trainees assigned to this trainer")
        List<TrainerProfileResponse.TraineeSummary> trainees

) {
}