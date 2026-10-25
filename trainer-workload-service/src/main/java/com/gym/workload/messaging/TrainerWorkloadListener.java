package com.gym.workload.messaging;

import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TrainerWorkloadListener {
    public static final String TRANSACTION_ID_PROPERTY = "transactionId";
    private static final Logger log = LoggerFactory.getLogger(TrainerWorkloadListener.class);

    private final TrainerWorkloadService service;
    private final Validator validator;
    private final JmsTemplate jmsTemplate;
    private final String deadLetterQueue;

    public TrainerWorkloadListener(TrainerWorkloadService service,
                                   Validator validator,
                                   JmsTemplate jmsTemplate,
                                   @Value("${messaging.workload.dead-letter-queue}") String deadLetterQueue) {
        this.service = service;
        this.validator = validator;
        this.jmsTemplate = jmsTemplate;
        this.deadLetterQueue = deadLetterQueue;
    }

    @JmsListener(destination = "${messaging.workload.queue}")
    public void receive(TrainerWorkloadRequest request, @Header(name = TRANSACTION_ID_PROPERTY, required = false) String transactionId) {
        if (transactionId != null && !transactionId.isBlank()) {
            MDC.put(TRANSACTION_ID_PROPERTY, transactionId);
        }
        try {
            log.info("Started workload event transaction for trainer = {}", request.trainerUsername());
            Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .sorted()
                        .collect(Collectors.joining("; "));
                log.warn("Routing invalid workload message to {}: {}", deadLetterQueue, errors);
                jmsTemplate.convertAndSend(deadLetterQueue, request, message -> {
                    message.setStringProperty("validationErrors", errors);
                    return message;
                });
                return;
            }
            service.applyWorkload(request);
            log.info("Completed workload event transaction for trainer = {}", request.trainerUsername());
        } finally {
            MDC.remove(TRANSACTION_ID_PROPERTY);
        }
    }
}
