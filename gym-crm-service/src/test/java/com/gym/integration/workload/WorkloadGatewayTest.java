package com.gym.integration.workload;

import com.gym.model.Trainer;
import com.gym.model.Training;
import com.gym.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class WorkloadGatewayTest {
    @Test
    void publishesWorkloadEventToConfiguredQueue() {
        JmsTemplate jmsTemplate = mock(JmsTemplate.class);
        WorkloadGateway gateway = new WorkloadGateway(jmsTemplate, "trainer.workload");
        Training training = mock(Training.class);
        Trainer trainer = mock(Trainer.class);
        User user = mock(User.class);
        when(training.getTrainer()).thenReturn(trainer);
        when(trainer.getUser()).thenReturn(user);
        when(user.getUsername()).thenReturn("nika.beridze");
        when(user.getFirstName()).thenReturn("Nika");
        when(user.getLastName()).thenReturn("Beridze");
        when(user.getIsActive()).thenReturn(true);
        when(training.getTrainingDate()).thenReturn(Instant.parse("2026-08-04T10:00:00Z"));
        when(training.getTrainingDuration()).thenReturn(60);

        gateway.send(training, TrainerWorkloadRequest.ActionType.ADD);

        verify(jmsTemplate).convertAndSend(eq("trainer.workload"), any(TrainerWorkloadRequest.class));
    }
}
