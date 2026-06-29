package com.gym.controller;

import com.gym.dto.ActivationRequest;
import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainee.*;
import com.gym.security.RequiresAuthentication;
import com.gym.service.TraineeService;
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

import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainees", description = "Trainee registration and profile management")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Operation(summary = "Register a new trainee", description = "Creates a trainee profile and auto-generates a unique username and password.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainee created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(traineeService.register(request));
    }

    @Operation(summary = "Get trainee profile", description = "Returns full trainee profile including assigned trainers. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        TraineeProfileResponse response = traineeService.getProfile(username);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainee profile", description = "Updates trainee details. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @RequiresAuthentication
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @PutMapping("/{username}")
    public ResponseEntity<TraineeUpdateResponse> update(
            @PathVariable String username,
            @Valid @RequestBody TraineeUpdateRequest request) {

        TraineeUpdateResponse response = traineeService.update(username, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Deletes a trainee and all associated data. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainee deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @RequiresAuthentication
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        traineeService.delete(username);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get active trainers not assigned to a trainee",
            description = "Returns active trainers that are not yet assigned to the given trainee. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TraineeProfileResponse.TrainerSummary>> getUnassignedTrainers(@PathVariable String username) {
        return ResponseEntity.ok(traineeService.getUnassignedTrainers(username));
    }

    @Operation(
            summary = "Update trainee's trainer list",
            description = "Replaces the full set of trainers assigned to a trainee. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainer list updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee or one or more trainers not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TraineeProfileResponse.TrainerSummary>> updateTrainers(
            @PathVariable String username,
            @Valid @RequestBody UpdateTraineeTrainersRequest request) {

        return ResponseEntity.ok(traineeService.updateTrainers(username, request));
    }

    @Operation(summary = "Activate or deactivate a trainee", description = "Sets the trainee's active state. Not idempotent. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active state updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> setActive(
            @PathVariable String username,
            @Valid @RequestBody ActivationRequest request) {

        traineeService.setActive(username, request.isActive());
        return ResponseEntity.ok().build();
    }

}