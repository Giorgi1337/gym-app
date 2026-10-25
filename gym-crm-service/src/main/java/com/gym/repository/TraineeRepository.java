package com.gym.repository;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUser_Username(String username);

    @Query("""
            SELECT tr FROM Trainer tr
            JOIN FETCH tr.user u
            WHERE tr NOT IN (
                SELECT assigned FROM Trainee t
                JOIN t.trainers assigned
                JOIN t.user tu
                WHERE tu.username = :username
            )
            """)
    List<Trainer> findUnassignedTrainers(@Param("username") String username);
}
