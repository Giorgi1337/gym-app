package com.gym.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Payload for replacing a trainee's assigned trainers")
public record UpdateTraineeTrainersRequest(

        @Schema(description = "Trainers to assign to the trainee")
        @NotEmpty(message = "Trainers list must not be empty")
        List<@Valid TrainerRef> trainers
) {
    @Schema(description = "Reference to a trainer by username")
    public record TrainerRef(

            @Schema(description = "Trainer's username", example = "Levan.Tsereteli")
            @NotBlank(message = "Trainer username is required")
            String trainerUsername
    ) {
    }
}