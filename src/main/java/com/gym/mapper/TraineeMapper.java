package com.gym.mapper;

import com.gym.dto.*;
import com.gym.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TraineeMapper {

    private TraineeMapper() {}

    public static Trainee toEntity(TraineeRegistrationRequest request) {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .isActive(true)
                        .build())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();
    }

    public static RegistrationResponse toRegistrationResponse(Trainee trainee) {
        return new RegistrationResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword()
        );
    }

    public static TraineeProfileResponse toProfileResponse(Trainee trainee) {
        User user = trainee.getUser();

        return new TraineeProfileResponse()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .isActive(user.getIsActive())
                .trainers(mapTrainers(trainee));
    }

    public static TraineeUpdateResponse toUpdateResponse(Trainee trainee) {
        User user = trainee.getUser();

        return new TraineeUpdateResponse()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(trainee.getDateOfBirth())
                .address(trainee.getAddress())
                .isActive(user.getIsActive())
                .trainers(mapTrainers(trainee));
    }

    public static void applyUpdate(Trainee trainee, TraineeUpdateRequest request) {
        User user = trainee.getUser();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(request.getIsActive());

        trainee.setDateOfBirth(request.getDateOfBirth());
        trainee.setAddress(request.getAddress());
    }

    private static List<TrainerSummary> mapTrainers(Trainee trainee) {
        if (trainee.getTrainers() == null || trainee.getTrainers().isEmpty()) {
            return Collections.emptyList();
        }

        return trainee.getTrainers()
                .stream()
                .filter(Objects::nonNull)
                .map(TraineeMapper::toTrainerSummary)
                .toList();
    }

    private static TrainerSummary toTrainerSummary(Trainer trainer) {
        User trainerUser = trainer.getUser();

        return new TrainerSummary()
                .username(trainerUser.getUsername())
                .firstName(trainerUser.getFirstName())
                .lastName(trainerUser.getLastName())
                .specialization(trainer.getSpecialization().getTrainingTypeName());
    }
}