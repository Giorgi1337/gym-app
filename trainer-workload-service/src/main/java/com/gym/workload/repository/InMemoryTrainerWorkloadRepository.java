package com.gym.workload.repository;

import com.gym.workload.model.TrainerWorkload;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTrainerWorkloadRepository {

    private final ConcurrentHashMap<String, TrainerWorkload> store = new ConcurrentHashMap<>();

    public TrainerWorkload findOrCreate(String username, String firstName, String lastName, boolean active) {
        return store.computeIfAbsent(username, _ -> new TrainerWorkload(username, firstName, lastName, active));
    }

    public TrainerWorkload find(String username) {
        return store.get(username);
    }

}
