package com.gym.workload.model;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class TrainerWorkload {

    private final String username;
    private volatile String firstName;
    private volatile String lastName;
    private volatile boolean active;

    // year -> month -> total minutes
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, AtomicInteger>> yearlyData = new ConcurrentHashMap<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    public synchronized void applyDelta(int year, int month, int minutesDelta) {
        AtomicInteger total = yearlyData
                .computeIfAbsent(year, y -> new ConcurrentHashMap<>())
                .computeIfAbsent(month, m -> new AtomicInteger(0));
        int updated = total.updateAndGet(current -> Math.max(0, current + minutesDelta));
        if (updated == 0) {
            yearlyData.get(year).remove(month, total);
            if (yearlyData.get(year).isEmpty()) {
                yearlyData.remove(year);
            }
        }
    }

    public void updateProfile(String firstName, String lastName, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
}
