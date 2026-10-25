package com.gym.workload.integration;

import com.gym.workload.dto.TrainerWorkloadRequest;
import com.gym.workload.model.TrainerWorkload;
import com.gym.workload.repository.TrainerWorkloadRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
class RunMicroserviceIntegrationTest {
    private static final String WORKLOAD_QUEUE = "trainer.workload";
    private static final String DEAD_LETTER_QUEUE = "trainer.workload.dlq";

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:4.4.29"));

    @Container
    @SuppressWarnings("resource") // The Testcontainers JUnit extension owns this container's lifecycle.
    static final GenericContainer<?> artemis = new GenericContainer<>(DockerImageName.parse("apache/activemq-artemis:2.44.0"))
            .withEnv("ARTEMIS_USER", "admin")
            .withEnv("ARTEMIS_PASSWORD", "admin")
            .withExposedPorts(61616)
            .waitingFor(Wait.forListeningPort());

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private JmsTemplate jmsTemplate;

    @DynamicPropertySource
    static void configureInfrastructure(DynamicPropertyRegistry properties) {
        properties.add("spring.mongodb.uri", mongo::getReplicaSetUrl);
        properties.add("spring.artemis.broker-url", () -> "tcp://" + artemis.getHost() + ":" + artemis.getMappedPort(61616));
        properties.add("spring.artemis.user", () -> "admin");
        properties.add("spring.artemis.password", () -> "admin");
    }

    @BeforeEach
    void clearState() {
        repository.deleteAll();
        jmsTemplate.setReceiveTimeout(Duration.ofSeconds(2).toMillis());
        Object staleMessage = jmsTemplate.receiveAndConvert(DEAD_LETTER_QUEUE);
        while (staleMessage != null) {
            staleMessage = jmsTemplate.receiveAndConvert(DEAD_LETTER_QUEUE);
        }
    }

    @Test
    void consumesWorkloadEventAndPersistsMonthlySummary() {
        var request = new TrainerWorkloadRequest(
                "Nika.Beridze", "Nika", "Beridze", true,
                LocalDate.of(2026, 8, 20), 45, TrainerWorkloadRequest.ActionType.ADD);

        jmsTemplate.convertAndSend(WORKLOAD_QUEUE, request);

        TrainerWorkload saved = awaitWorkload("Nika.Beridze");
        assertThat(saved.getYears()).singleElement().satisfies(year -> {
            assertThat(year.getYear()).isEqualTo(2026);
            assertThat(year.getMonths()).singleElement().satisfies(month -> {
                assertThat(month.getMonth()).isEqualTo(8);
                assertThat(month.getTrainingSummaryDuration()).isEqualTo(45);
            });
        });
    }

    @Test
    void routesInvalidWorkloadEventToDeadLetterQueue() {
        var invalidRequest = new TrainerWorkloadRequest(
                " ", "Nika", "Beridze", true,
                LocalDate.of(2026, 8, 20), 0, TrainerWorkloadRequest.ActionType.ADD);

        jmsTemplate.convertAndSend(WORKLOAD_QUEUE, invalidRequest);

        Object deadLetter = jmsTemplate.receiveAndConvert(DEAD_LETTER_QUEUE);
        assertThat(deadLetter).isInstanceOf(TrainerWorkloadRequest.class)
                .extracting(value -> ((TrainerWorkloadRequest) value).trainerUsername()).isEqualTo(" ");
        assertThat(repository.findByUsername(" ")).isEmpty();
    }

    private TrainerWorkload awaitWorkload(String username) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(repository.findByUsername(username)).isPresent());
        return repository.findByUsername(username).orElseThrow();
    }
}
