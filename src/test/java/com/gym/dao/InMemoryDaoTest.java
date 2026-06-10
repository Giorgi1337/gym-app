package com.gym.dao;

import com.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

public class InMemoryDaoTest {

    private InMemoryDao<Trainee, Long> dao;
    private Map<Long, Trainee> storage;

    @BeforeEach
    void setUp() {
        storage = new ConcurrentHashMap<>();
        dao = new InMemoryDao<>(storage);
    }

    @Test
    void save() {
        Trainee trainee = buildTrainee(1L, "John", "Smith");

        dao.save(1L, trainee);

        assertThat(trainee).isEqualTo(storage.get(1L));
    }

    @Test
    void update() {
        Trainee original = buildTrainee(1L, "John", "Smith");
        Trainee updated = buildTrainee(1L, "John", "Doe");

        dao.save(1L, original);
        dao.save(1L, updated);

        assertThat(storage.get(1L).getLastName()).isEqualTo("Doe");
    }


    @Test
    void findById() {
        Trainee trainee = buildTrainee(32L, "John", "Smith");
        storage.put(32L, trainee);

        assertThat(dao.findById(32L)).isEqualTo(trainee);
    }

    @Test
    void findByIdNotFound() {
        assertThat(dao.findById(900L)).isNull();
    }

    @Test
    void findAll() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));
        storage.put(2L, buildTrainee(2L, "Alice", "Jones"));

        assertThat(dao.findAll().size()).isEqualTo(2);
    }

    @Test
    void findAllNotFound() {
        assertThat(dao.findAll().isEmpty());
    }

    @Test
    void deleteById() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));

        dao.delete(1L);

        assertThat(storage.get(1L)).isNull();
    }

    @Test
    void deleteByIdNotFound() {
        assertThatCode(() -> dao.delete(999L)).doesNotThrowAnyException();
    }

    @Test
    void existsById() {
        storage.put(1L, buildTrainee(1L, "John", "Smith"));

        assertThat(dao.exists(1L)).isTrue();
    }

    @Test
    void existsByIdNotFound() {
        assertThat(dao.exists(999L)).isFalse();
    }

    private Trainee buildTrainee(Long id, String firstName, String lastName) {
        return Trainee.builder()
                .userId(id)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

}