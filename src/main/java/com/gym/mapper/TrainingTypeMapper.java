package com.gym.mapper;

import com.gym.dto.training.TrainingTypeResponse;
import com.gym.model.TrainingType;

public final class TrainingTypeMapper {

    private TrainingTypeMapper() {}

    public static TrainingTypeResponse toResponse(TrainingType trainingType) {
        return new TrainingTypeResponse(
                trainingType.getId(),
                trainingType.getTrainingTypeName()
        );
    }
}