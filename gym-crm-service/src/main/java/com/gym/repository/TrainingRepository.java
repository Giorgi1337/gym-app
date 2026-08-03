package com.gym.repository;

import com.gym.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long>, JpaSpecificationExecutor<Training> {
    @Query("""
            SELECT t FROM Training t
            WHERE t.trainer.user.username = :trainerUsername
              AND t.trainee.user.username = :traineeUsername
              AND trim(t.trainingName) = :trainingName
              AND t.trainingDate = :trainingDate
              AND t.trainingDuration = :trainingDuration
            ORDER BY t.id
            """)
    List<Training> findMatchingTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("traineeUsername") String traineeUsername,
            @Param("trainingName") String trainingName,
            @Param("trainingDate") Instant trainingDate,
            @Param("trainingDuration") Integer trainingDuration);
}
