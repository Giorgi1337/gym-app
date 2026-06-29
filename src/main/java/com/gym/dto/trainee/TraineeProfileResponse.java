package com.gym.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Trainee profile details")
public record TraineeProfileResponse(

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
        List<TrainerSummary> trainers

) {
    @Schema(description = "Summary of a trainer assigned to a trainee")
    public record TrainerSummary(

            @Schema(description = "Trainer's username", example = "Levan.Tsereteli")
            String username,

            @Schema(description = "Trainer's first name", example = "Levan")
            String firstName,

            @Schema(description = "Trainer's last name", example = "Tsereteli")
            String lastName,

            @Schema(description = "Trainer's specialization (training type name)", example = "Yoga")
            String specialization

    ) {
    }
}