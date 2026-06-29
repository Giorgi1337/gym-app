package com.gym.mapper;

import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainee.TraineeProfileResponse.TrainerSummary;
import com.gym.dto.trainer.TrainerProfileResponse;
import com.gym.dto.trainer.TrainerProfileResponse.TraineeSummary;
import com.gym.dto.trainer.TrainerRegistrationRequest;
import com.gym.dto.trainer.TrainerUpdateRequest;
import com.gym.dto.trainer.TrainerUpdateResponse;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;

public final class TrainerMapper {

    private TrainerMapper() {}

    public static Trainer toEntity(TrainerRegistrationRequest request) {
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .isActive(true)
                .build();

        TrainingType specialization = TrainingType.builder()
                .trainingTypeName(request.specialization())
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
        return new TrainerSummary(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public static TrainerProfileResponse toProfileResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                user.getIsActive(),
                trainer.getTrainees().stream()
                        .map(TrainerMapper::toTraineeSummary)
                        .toList()
        );
    }

    public static void applyUpdate(Trainer trainer, TrainerUpdateRequest request) {
        User user = trainer.getUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setIsActive(request.isActive());
    }

    public static TrainerUpdateResponse toUpdateResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerUpdateResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                user.getIsActive(),
                trainer.getTrainees().stream()
                        .map(TrainerMapper::toTraineeSummary)
                        .toList()
        );
    }

    private static TraineeSummary toTraineeSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummary(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}