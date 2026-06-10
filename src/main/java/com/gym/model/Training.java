package com.gym.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Training {

    @JsonProperty("traineeId")
    private Long traineeId;

    @JsonProperty("trainerId")
    private Long trainerId;

    @JsonProperty("trainingName")
    private String trainingName;

    @JsonProperty("trainingType")
    private TrainingType trainingType;

    @JsonProperty("trainingDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate trainingDate;

    @JsonProperty("trainingDurationMinutes")
    private int trainingDurationMinutes;

    @Override
    public String toString() {
        return "Training[name='%s' | trainerId=%d | traineeId=%d | type=%s | date=%s | duration=%dmin]"
                .formatted(
                        trainingName,
                        trainerId,
                        traineeId,
                        trainingType,
                        trainingDate,
                        trainingDurationMinutes
                );
    }
}