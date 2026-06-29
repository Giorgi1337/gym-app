package com.gym.dto.training;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Training type reference entry")
public record TrainingTypeResponse(

        @Schema(description = "Training type id", example = "1")
        Long id,

        @Schema(description = "Training type name", example = "Yoga")
        String trainingTypeName

) {
}