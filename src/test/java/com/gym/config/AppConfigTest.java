package com.gym.config;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AppConfigTest {
    private AnnotationConfigApplicationContext ctx;

    @BeforeEach
    void setUp() {
        ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @AfterEach
    void tearDown() {
        ctx.close();
    }

    @Test
    void contextLoadsAllBeansSuccessfully() {
        assertThat(ctx.getBean("traineeStorage")).isNotNull();
        assertThat(ctx.getBean("trainerStorage")).isNotNull();
        assertThat(ctx.getBean("trainingStorage")).isNotNull();
        assertThat(ctx.getBean("traineeDao")).isNotNull();
        assertThat(ctx.getBean("trainerDao")).isNotNull();
        assertThat(ctx.getBean("trainingDao")).isNotNull();
    }
    @Test
    void storageInitializerThrowsExceptionWhenFileNotFound() {
        try (var testCtx = new AnnotationConfigApplicationContext()) {
            testCtx.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource(
                            "test",
                            Map.of(
                                    "storage.trainee.path",  "classpath:data/nonexistent.json",
                                    "storage.trainer.path",  "classpath:data/nonexistent.json",
                                    "storage.training.path", "classpath:data/nonexistent.json"
                            )
                    )
            );
            testCtx.register(AppConfig.class);

            assertThatThrownBy(testCtx::refresh)
                    .isInstanceOf(Exception.class)
                    .hasMessageContaining("Failed to initialize storage");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Trainee> traineeStorage() {
        return (Map<Long, Trainee>) ctx.getBean("traineeStorage");
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Trainer> trainerStorage() {
        return (Map<Long, Trainer>) ctx.getBean("trainerStorage");
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Training> trainingStorage() {
        return (Map<Long, Training>) ctx.getBean("trainingStorage");
    }
}
