package com.gym.mapper;

import com.gym.dto.TrainingTypeResponse;
import com.gym.model.TrainingType;

public final class TrainingTypeMapper {

    private TrainingTypeMapper() {}

    public static TrainingTypeResponse toResponse(TrainingType trainingType) {
        return new TrainingTypeResponse()
                .id(trainingType.getId())
                .trainingTypeName(trainingType.getTrainingTypeName());
    }
}