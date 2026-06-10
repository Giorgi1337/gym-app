package com.gym.config;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class StorageInitializer implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);

    private final ObjectMapper objectMapper;
    private final Environment environment;

    public StorageInitializer(ObjectMapper objectMapper, Environment environment) {
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) {

        try {

            // TRAINEE STORAGE
            if (beanName.equals("traineeStorage")) {
                Map<Long, Trainee> storage = (Map<Long, Trainee>) bean;

                String path = environment.getProperty("storage.trainee.path");

                List<Trainee> data = loadList(path, new TypeReference<>() {});
                data.forEach(t -> storage.put(t.getUserId(), t));
                log.info("Loaded {} trainee data", storage.size());
            }

            // TRAINER STORAGE
            if (beanName.equals("trainerStorage")) {
                Map<Long, Trainer> storage = (Map<Long, Trainer>) bean;

                String path = environment.getProperty("storage.trainer.path");

                List<Trainer> data = loadList(path, new TypeReference<>() {});
                data.forEach(t -> storage.put(t.getUserId(), t));
                log.info("Loaded {} trainer data", storage.size());
            }

            // TRAINING STORAGE
            if (beanName.equals("trainingStorage")) {
                Map<Long, Training> storage = (Map<Long, Training>) bean;

                String path = environment.getProperty("storage.training.path");

                List<Training> data = loadList(path, new TypeReference<>() {});
                long id = 1;

                for (Training t : data) {
                    storage.put(id++, t);
                }
                log.info("Loaded {} training data", storage.size());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize storage: " + beanName, e);
        }

        return bean;
    }

    private <T> List<T> loadList(String path, TypeReference<List<T>> typeRef) throws Exception {

        if (path == null) return List.of();

        InputStream is = new ClassPathResource(path.replace("classpath:", "")).getInputStream();

        return objectMapper.readValue(is, typeRef);
    }
}