package com.gym.dto.trainer;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Trainer profile details")
public record TrainerProfileResponse(

        @Schema(description = "First name", example = "Levan")
        String firstName,

        @Schema(description = "Last name", example = "Tsereteli")
        String lastName,

        @Schema(description = "Specialization (training type name)", example = "Yoga")
        String specialization,

        @Schema(description = "Whether the trainer account is active", example = "true")
        Boolean isActive,

        @Schema(description = "Trainees assigned to this trainer")
        List<TraineeSummary> trainees

) {
    @Schema(description = "Summary of a trainee assigned to a trainer")
    public record TraineeSummary(

            @Schema(description = "Trainee's username", example = "Nika.Beridze")
            String username,

            @Schema(description = "Trainee's first name", example = "Nika")
            String firstName,

            @Schema(description = "Trainee's last name", example = "Beridze")
            String lastName

    ) {
    }
}