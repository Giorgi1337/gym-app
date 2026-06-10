package com.gym.config;

import com.gym.dao.Dao;
import com.gym.dao.InMemoryDao;
import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@ComponentScan(basePackages = "com.gym")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean
    public Map<Long, Trainee> traineeStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, Trainer> trainerStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, Training> trainingStorage() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Dao<Trainee, Long> traineeDao(Map<Long, Trainee> traineeStorage) {
        return new InMemoryDao<>(traineeStorage);
    }

    @Bean
    public Dao<Trainer, Long> trainerDao(Map<Long, Trainer> trainerStorage) {
        return new InMemoryDao<>(trainerStorage);
    }

    @Bean
    public Dao<Training, Long> trainingDao(Map<Long, Training> trainingStorage) {
        return new InMemoryDao<>(trainingStorage);
    }
}
