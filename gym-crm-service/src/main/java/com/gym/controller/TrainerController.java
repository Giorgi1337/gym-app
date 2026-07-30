package com.gym.controller;

import com.gym.api.TrainersApi;

import com.gym.dto.*;
import com.gym.service.TrainerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class TrainerController implements TrainersApi {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    public ResponseEntity<RegistrationResponse> registerTrainer(TrainerRegistrationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trainerService.save(request));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(String username) {
        return ResponseEntity.ok(trainerService.findByUsername(username));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(String username, TrainerUpdateRequest request) {
        return ResponseEntity.ok(trainerService.update(username, request));
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> setTrainerActive(String username, ActivationRequest request) {
        trainerService.setActive(username, request.getIsActive());
        return ResponseEntity.ok().build();
    }
}