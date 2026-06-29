package com.gym.mapper;

import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainee.TraineeProfileResponse;
import com.gym.dto.trainee.TraineeRegistrationRequest;
import com.gym.dto.trainee.TraineeUpdateRequest;
import com.gym.dto.trainee.TraineeUpdateResponse;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;

import java.util.List;

public final class TraineeMapper {

    private TraineeMapper() {}

    public static Trainee toEntity(TraineeRegistrationRequest request) {
        return Trainee.builder()
                .user(User.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .isActive(true)
                        .build())
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
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

        List<TraineeProfileResponse.TrainerSummary> trainerSummaries = trainee.getTrainers().stream()
                .map(TraineeMapper::toTrainerSummary)
                .toList();

        return new TraineeProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                user.getIsActive(),
                trainerSummaries
        );
    }

    private static TraineeProfileResponse.TrainerSummary toTrainerSummary(Trainer trainer) {
        User trainerUser = trainer.getUser();
        return new TraineeProfileResponse.TrainerSummary(
                trainerUser.getUsername(),
                trainerUser.getFirstName(),
                trainerUser.getLastName(),
                trainer.getSpecialization().getTrainingTypeName()
        );
    }

    public static void applyUpdate(Trainee trainee, TraineeUpdateRequest request) {
        User user = trainee.getUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setIsActive(request.isActive());

        trainee.setDateOfBirth(request.dateOfBirth());
        trainee.setAddress(request.address());
    }

    public static TraineeUpdateResponse toUpdateResponse(Trainee trainee) {
        User user = trainee.getUser();

        List<TraineeProfileResponse.TrainerSummary> trainerSummaries = trainee.getTrainers().stream()
                .map(TraineeMapper::toTrainerSummary)
                .toList();

        return new TraineeUpdateResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                user.getIsActive(),
                trainerSummaries
        );
    }

}