package com.gym.controller;

import com.gym.dto.training.AddTrainingRequest;
import com.gym.dto.training.TraineeTrainingResponse;
import com.gym.dto.training.TrainerTrainingResponse;
import com.gym.dto.training.TrainingTypeResponse;
import com.gym.security.RequiresAuthentication;
import com.gym.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "Trainings", description = "Training scheduling and history")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(summary = "Get trainee's trainings list", description = "Returns trainings for a trainee, optionally filtered. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @GetMapping("/api/trainees/{username}/trainings")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        return ResponseEntity.ok(
                trainingService.getTraineeTrainings(username, periodFrom, periodTo, trainerName, trainingType));
    }

    @Operation(summary = "Get trainer's trainings list", description = "Returns trainings for a trainer, optionally filtered. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @GetMapping("/api/trainers/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(required = false) String traineeName) {

        return ResponseEntity.ok(
                trainingService.getTrainerTrainings(username, periodFrom, periodTo, traineeName));
    }

    @Operation(
            summary = "Add a training",
            description = "Schedules a new training, expressed as a sub-resource of the trainer-trainee relationship. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training added successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required or invalid credentials"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer not found")
    })
    @Parameter(name = "X-Username", description = "Authenticated user's username", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Password", description = "Authenticated user's password", required = true, in = ParameterIn.HEADER)
    @RequiresAuthentication
    @PostMapping("/api/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings")
    public ResponseEntity<Void> addTraining(
            @PathVariable String trainerUsername,
            @PathVariable String traineeUsername,
            @Valid @RequestBody AddTrainingRequest request) {

        trainingService.addTraining(trainerUsername, traineeUsername, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get training types", description = "Returns the constant list of available training types.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    })
    @GetMapping("/api/trainings/types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(trainingService.getTrainingTypes());
    }
}