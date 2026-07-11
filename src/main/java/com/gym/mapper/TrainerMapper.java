package com.gym.mapper;

import com.gym.dto.*;
import com.gym.model.*;

public final class TrainerMapper {

    private TrainerMapper() {
    }

    public static Trainer toEntity(TrainerRegistrationRequest request) {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .isActive(true)
                .build();

        TrainingType specialization = TrainingType.builder()
                .trainingTypeName(request.getSpecialization())
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();
    }

    public static RegistrationResponse toRegistrationResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new RegistrationResponse(user.getUsername(), user.getPassword());
    }

    public static TrainerSummary toSummary(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerSummary()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName());
    }

    public static TrainerProfileResponse toProfileResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerProfileResponse()
                .firstName(user.getFirstName())
                .lastName(user.getFirstName())
                .lastName(user.getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName())
                .isActive(user.getIsActive())
                .trainees(trainer.getTrainees()
                        .stream()
                        .map(TrainerMapper::toTraineeSummary)
                        .toList()
                );
    }

    public static void applyUpdate(Trainer trainer, TrainerUpdateRequest request) {
        User user = trainer.getUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());
    }

    public static TrainerUpdateResponse toUpdateResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerUpdateResponse()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName())
                .isActive(user.getIsActive())
                .trainees(trainer.getTrainees().stream()
                        .map(TrainerMapper::toTraineeSummary)
                        .toList());
    }

    private static TraineeSummary toTraineeSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummary()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName());
    }
}