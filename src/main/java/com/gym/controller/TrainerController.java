package com.gym.controller;

import com.gym.dto.ActivationRequest;
import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainer.TrainerProfileResponse;
import com.gym.dto.trainer.TrainerRegistrationRequest;
import com.gym.dto.trainer.TrainerUpdateRequest;
import com.gym.dto.trainer.TrainerUpdateResponse;
import com.gym.security.RequiresAuthentication;
import com.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainers", description = "Trainer registration and profile management")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping
    @Operation(
            summary = "Register a new trainer",
            description = "Creates a trainer profile and auto-generates a unique username and password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Specialization not found")
    })
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trainerService.register(request));
    }

    @Operation(summary = "Get trainer profile", description = "Returns full trainer profile including assigned trainees. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(trainerService.getProfile(username));
    }

    @Operation(summary = "Update trainer profile", description = "Updates trainer details. Specialization is read-only and cannot be changed. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @PutMapping("/{username}")
    public ResponseEntity<TrainerUpdateResponse> update(
            @PathVariable String username,
            @Valid @RequestBody TrainerUpdateRequest request) {

        return ResponseEntity.ok(trainerService.update(username, request));
    }

    @Operation(summary = "Activate or deactivate a trainer", description = "Sets the trainer's active state. Not idempotent. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active state updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequest request) {

        trainerService.setActive(username, request.isActive());
        return ResponseEntity.ok().build();
    }
}