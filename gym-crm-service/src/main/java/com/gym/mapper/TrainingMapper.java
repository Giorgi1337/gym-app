package com.gym.mapper;

import com.gym.dto.*;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.gym.utils.NameUtils.fullName;

public final class TrainingMapper {

    private TrainingMapper() {}

    public static Training toEntity(AddTrainingRequest request, Trainee trainee, Trainer trainer) {
        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.getTrainingDate().toInstant())
                .trainingDuration(request.getTrainingDuration())
                .build();
    }

    public static TraineeTrainingResponse toTraineeView(Training training) {
        return new TraineeTrainingResponse()
                .trainingName(training.getTrainingName())
                .trainingDate(toUtcOffsetDateTime(training.getTrainingDate()))
                .trainingType(training.getTrainingType().getTrainingTypeName())
                .trainingDuration(training.getTrainingDuration())
                .trainerName(fullName(
                        training.getTrainer().getUser().getFirstName(),
                        training.getTrainer().getUser().getLastName()
                ));
    }

    public static TrainerTrainingResponse toTrainerView(Training training) {
        return new TrainerTrainingResponse()
                .trainingName(training.getTrainingName())
                .trainingDate(toUtcOffsetDateTime(training.getTrainingDate()))
                .trainingType(training.getTrainingType().getTrainingTypeName())
                .trainingDuration(training.getTrainingDuration())
                .traineeName(fullName(
                        training.getTrainee().getUser().getFirstName(),
                        training.getTrainee().getUser().getLastName()
                ));
    }

    public static TraineeTrainingPageResponse toTraineePage(Page<Training> page) {
        return new TraineeTrainingPageResponse()
                .content(
                        page.getContent()
                                .stream()
                                .map(TrainingMapper::toTraineeView)
                                .toList()
                )
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }

    public static TrainerTrainingPageResponse toTrainerPage(Page<Training> page) {
        return new TrainerTrainingPageResponse()
                .content(
                        page.getContent()
                                .stream()
                                .map(TrainingMapper::toTrainerView)
                                .toList()
                )
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages());
    }

    private static OffsetDateTime toUtcOffsetDateTime(Instant dateTime) {
        return dateTime.atOffset(ZoneOffset.UTC);
    }
}
