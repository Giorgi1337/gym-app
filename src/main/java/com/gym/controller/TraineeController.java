package com.gym.controller;

import com.gym.api.TraineesApi;
import com.gym.dto.*;
import com.gym.dto.TrainerSummary;
import com.gym.service.TraineeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TraineeController implements TraineesApi {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    public ResponseEntity<RegistrationResponse> registerTrainee(TraineeRegistrationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(traineeService.save(request));
    }

    public ResponseEntity<TraineeProfileResponse> getProfile(String username) {
        return ResponseEntity.ok(traineeService.findByUsername(username));
    }

    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(String username, TraineeUpdateRequest request) {
        return ResponseEntity.ok(traineeService.update(username, request));
    }

    public ResponseEntity<Void> deleteTraineeProfile(String username) {
        traineeService.delete(username);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<List<TrainerSummary>> getUnassignedTrainers(String username) {
        return ResponseEntity.ok(traineeService.getUnassignedTrainers(username));
    }

    public ResponseEntity<List<TrainerSummary>> updateTrainers(String username, UpdateTraineeTrainersRequest updateTraineeTrainersRequest) {
        return ResponseEntity.ok(traineeService.updateTrainers(username, updateTraineeTrainersRequest));
    }

    public ResponseEntity<Void> setActive(String username, ActivationRequest request) {
        traineeService.setActive(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }

}