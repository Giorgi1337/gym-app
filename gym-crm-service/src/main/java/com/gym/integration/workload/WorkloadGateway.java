package com.gym.integration.workload;

import com.gym.exception.DownstreamServiceUnavailableException;
import com.gym.exception.DownstreamRequestRejectedException;
import com.gym.model.Training;
import com.gym.model.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
public class WorkloadGateway {
    private final WorkloadClient client;

    public WorkloadGateway(WorkloadClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "trainerWorkload", fallbackMethod = "sendFallback")
    @Retry(name = "trainerWorkload")
    public void send(Training training, TrainerWorkloadRequest.ActionType actionType) {
        User user = training.getTrainer().getUser();
        var request = new TrainerWorkloadRequest(
                user.getUsername(), user.getFirstName(), user.getLastName(), user.getIsActive(),
                training.getTrainingDate().atZone(ZoneOffset.UTC).toLocalDate(),
                training.getTrainingDuration(), actionType);
        client.applyWorkload(user.getUsername(), request);
    }

    private void sendFallback(Throwable cause) {
        FeignException feignException = findFeignException(cause);
        if (feignException != null && feignException.status() >= 400 && feignException.status() < 500) {
            throw new DownstreamRequestRejectedException(
                    "Trainer workload service rejected the synchronization request", cause);
        }
        throw new DownstreamServiceUnavailableException(
                "Trainer workload service is temporarily unavailable", cause);
    }

    private FeignException findFeignException(Throwable cause) {
        Throwable current = cause;
        while (current != null) {
            if (current instanceof FeignException feignException) {
                return feignException;
            }
            current = current.getCause();
        }
        return null;
    }

}
