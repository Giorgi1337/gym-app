package com.gym.repository;


import com.gym.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUser_Username(String username);

    @Query("""
            SELECT t
            FROM Trainer t
            JOIN FETCH t.user u
            WHERE u.username IN :usernames
            """)
    List<Trainer> findByUsernames(@Param("usernames") Set<String> usernames);

    long countByUser_IsActiveTrue();
}
