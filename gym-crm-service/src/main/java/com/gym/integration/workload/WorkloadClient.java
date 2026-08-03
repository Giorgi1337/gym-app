package com.gym.integration.workload;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload-service")
public interface WorkloadClient {
    @PostMapping("/api/trainers/{username}/workload")
    ResponseEntity<Void> applyWorkload(@PathVariable("username") String username, @RequestBody TrainerWorkloadRequest request);
}
