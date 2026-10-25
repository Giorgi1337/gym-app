package com.gym.controller;

import com.gym.api.TraineesApi;
import com.gym.dto.*;
import com.gym.dto.TrainerSummary;
import com.gym.service.TraineeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TraineeProfileResponse> getProfile(String username) {
        return ResponseEntity.ok(traineeService.findByUsername(username));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(String username, TraineeUpdateRequest request) {
        return ResponseEntity.ok(traineeService.update(username, request));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> deleteTraineeProfile(String username) {
        traineeService.delete(username);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<List<TrainerSummary>> getUnassignedTrainers(String username) {
        return ResponseEntity.ok(traineeService.getUnassignedTrainers(username));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<List<TrainerSummary>> updateTrainers(String username, UpdateTraineeTrainersRequest updateTraineeTrainersRequest) {
        return ResponseEntity.ok(traineeService.updateTrainers(username, updateTraineeTrainersRequest));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> setActive(String username, ActivationRequest request) {
        traineeService.setActive(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }

}