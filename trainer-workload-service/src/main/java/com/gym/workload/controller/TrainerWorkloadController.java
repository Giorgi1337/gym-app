package com.gym.workload.controller;

import com.gym.workload.dto.TrainerMonthlySummaryResponse;
import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainers/{username}/workload")
public class TrainerWorkloadController {

    private final TrainerWorkloadService service;

    public TrainerWorkloadController(TrainerWorkloadService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_workload.write')")
    public ResponseEntity<Void> applyWorkload(@PathVariable String username, @Valid @RequestBody TrainerWorkloadRequest request) {

        if (!username.equals(request.trainerUsername())) {
            throw new IllegalArgumentException(
                    "Path username '%s' does not match request trainerUsername '%s'"
                            .formatted(username, request.trainerUsername()));
        }

        service.applyWorkload(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('SCOPE_workload.read.any') or " + "(hasAuthority('SCOPE_workload.read.self') and #username == authentication.name)")
    public ResponseEntity<TrainerMonthlySummaryResponse> getSummary(@PathVariable String username) {
        return ResponseEntity.ok(service.getSummary(username));
    }

}
