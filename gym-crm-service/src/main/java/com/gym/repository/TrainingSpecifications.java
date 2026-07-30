package com.gym.repository;

import com.gym.model.Training;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public final class TrainingSpecifications {

    private TrainingSpecifications() {}

    public static Specification<Training> forTrainee(String traineeUsername) {
        return (root, query, cb) ->
                cb.equal(root.get("trainee").get("user").get("username"), traineeUsername);
    }

    public static Specification<Training> forTrainer(String trainerUsername) {
        return (root, query, cb) ->
                cb.equal(root.get("trainer").get("user").get("username"), trainerUsername);
    }

    public static Specification<Training> dateFrom(LocalDate fromDate) {
        return fromDate == null
                ? null
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("trainingDate"), fromDate);
    }

    public static Specification<Training> dateTo(LocalDate toDate) {
        return toDate == null
                ? null
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("trainingDate"), toDate);
    }

    public static Specification<Training> trainerUsernameEquals(String trainerUsername) {
        return !StringUtils.hasText(trainerUsername)
                ? null
                : (root, query, cb) -> cb.equal(root.get("trainer").get("user").get("username"), trainerUsername);
    }

    public static Specification<Training> traineeUsernameEquals(String traineeUsername) {
        return !StringUtils.hasText(traineeUsername)
                ? null
                : (root, query, cb) -> cb.equal(root.get("trainee").get("user").get("username"), traineeUsername);
    }

    public static Specification<Training> trainingTypeEquals(String trainingType) {
        return !StringUtils.hasText(trainingType)
                ? null
                : (root, query, cb) -> cb.equal(cb.lower(root.get("trainingType").get("trainingTypeName")), trainingType.toLowerCase());
    }
}