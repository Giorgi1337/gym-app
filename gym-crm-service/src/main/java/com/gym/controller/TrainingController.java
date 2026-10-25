package com.gym.controller;

import com.gym.api.TrainingsApi;
import com.gym.dto.*;
import com.gym.service.TrainingService;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class TrainingController implements TrainingsApi {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TraineeTrainingPageResponse> getTraineeTrainings(
            String username,
            @Nullable OffsetDateTime periodFrom,
            @Nullable OffsetDateTime periodTo,
            @Nullable String trainerName,
            @Nullable String trainingType,
            Pageable pageable) {

        return ResponseEntity.ok(
                trainingService.getTraineeTrainings(
                        username, periodFrom, periodTo, trainerName, trainingType, pageable));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TrainerTrainingPageResponse> getTrainerTrainings(
            String username,
            @Nullable OffsetDateTime periodFrom,
            @Nullable OffsetDateTime periodTo,
            @Nullable String traineeName,
            Pageable pageable) {

        return ResponseEntity.ok(
                trainingService.getTrainerTrainings(username, periodFrom, periodTo, traineeName, pageable));
    }

    @PreAuthorize("#trainerUsername == authentication.name")
    public ResponseEntity<Void> addTraining(
            String trainerUsername,
            String traineeUsername,
            AddTrainingRequest addTrainingRequest) {

        trainingService.addTraining(trainerUsername, traineeUsername, addTrainingRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#trainerUsername == authentication.name")
    public ResponseEntity<Void> deleteTraining(
            String trainerUsername,
            String traineeUsername,
            AddTrainingRequest addTrainingRequest) {

        trainingService.deleteTraining(trainerUsername, traineeUsername, addTrainingRequest);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(trainingService.getTrainingTypes());
    }
}