package com.gym.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TrainingType {

    CARDIO("Cardio"),
    STRENGTH("Strength Training"),
    YOGA("Yoga"),
    PILATES("Pilates"),
    CROSSFIT("CrossFit"),
    BOXING("Boxing"),
    POWERLIFTING("Powerlifting");

    @JsonValue
    private final String displayName;

    TrainingType(String displayName) {
        this.displayName = displayName;
    }

    @JsonCreator
    public static TrainingType fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(t -> t.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown training type: " + displayName));
    }

    @Override
    public String toString() {
        return displayName;
    }
}