package com.gym.workload.messaging;

import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.service.TrainerWorkloadService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TrainerWorkloadListenerTest {
    private TrainerWorkloadService service;
    private JmsTemplate jmsTemplate;
    private TrainerWorkloadListener listener;

    @BeforeEach
    void setUp() {
        service = mock(TrainerWorkloadService.class);
        jmsTemplate = mock(JmsTemplate.class);
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        listener = new TrainerWorkloadListener(service, validator, jmsTemplate, "trainer.workload.dlq");
    }

    @Test
    void validMessageUpdatesWorkload() {
        var request = new TrainerWorkloadRequest(
                "nika.beridze", "Nika", "Beridze", true,
                LocalDate.of(2026, 8, 4), 60, TrainerWorkloadRequest.ActionType.ADD);

        listener.receive(request);

        verify(service).applyWorkload(request);
        verifyNoInteractions(jmsTemplate);
    }

    @Test
    void invalidMessageIsRoutedToDeadLetterQueue() {
        var request = new TrainerWorkloadRequest(
                null, "Nika", "Beridze", true,
                LocalDate.of(2026, 8, 4), 0, null);

        listener.receive(request);

        verifyNoInteractions(service);
        verify(jmsTemplate).convertAndSend(
                eq("trainer.workload.dlq"), eq(request), any(MessagePostProcessor.class));
    }
}
