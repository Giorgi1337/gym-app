package com.gym.dao;

import com.gym.model.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TrainerDao {

    private final SessionFactory sessionFactory;

    public TrainerDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
    }

    public Optional<Trainer> findByUserName(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                                SELECT t 
                                FROM  Trainer t 
                                JOIN FETCH t.user u 
                                WHERE u.username = :username
                                """,
                        Trainer.class
                )
                .setParameter("username", username)
                .uniqueResultOptional();
    }

}
