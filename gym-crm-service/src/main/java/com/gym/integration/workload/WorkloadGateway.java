package com.gym.integration.workload;

import com.gym.model.Training;
import com.gym.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
public class WorkloadGateway {
    private final JmsTemplate jmsTemplate;
    private final String destination;

    public WorkloadGateway(JmsTemplate jmsTemplate, @Value("${messaging.workload.queue}") String destination) {
        this.jmsTemplate = jmsTemplate;
        this.destination = destination;
    }

    public void send(Training training, TrainerWorkloadRequest.ActionType actionType) {
        User user = training.getTrainer().getUser();
        var request = new TrainerWorkloadRequest(
                user.getUsername(), user.getFirstName(), user.getLastName(), user.getIsActive(),
                training.getTrainingDate().atZone(ZoneOffset.UTC).toLocalDate(),
                training.getTrainingDuration(), actionType);
        jmsTemplate.convertAndSend(destination, request);
    }

}
