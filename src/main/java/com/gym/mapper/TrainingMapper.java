package com.gym.mapper;

import com.gym.dto.training.AddTrainingRequest;
import com.gym.dto.training.TraineeTrainingResponse;
import com.gym.dto.training.TrainerTrainingResponse;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;

import static com.gym.utils.NameUtils.fullName;

public final class TrainingMapper {

    private TrainingMapper() {}

    public static Training toEntity(AddTrainingRequest request, Trainee trainee, Trainer trainer) {
        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.trainingName())
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();
    }

    public static TraineeTrainingResponse toTraineeView(Training training) {
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                fullName(
                        training.getTrainer().getUser().getFirstName(),
                        training.getTrainer().getUser().getLastName()
                )
        );
    }

    public static TrainerTrainingResponse toTrainerView(Training training) {
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                fullName(
                        training.getTrainee().getUser().getFirstName(),
                        training.getTrainee().getUser().getLastName()
                )
        );
    }
}