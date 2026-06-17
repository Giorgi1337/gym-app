package com.gym.dao;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {

    private final SessionFactory sessionFactory;

    public TraineeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainee trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
    }

    public Optional<Trainee> findByUserName(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                                SELECT t
                                FROM Trainee t 
                                JOIN FETCH t.user u
                                WHERE u.username = :username
                                """,
                        Trainee.class
                )
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    public void delete(Trainee trainee) {
        sessionFactory.getCurrentSession().remove(trainee);
    }

    public List<Trainer> findUnassignedTrainers(String traineeUsername) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                        SELECT tr FROM Trainer tr
                        JOIN FETCH tr.user u
                        WHERE tr NOT IN (
                            SELECT assigned FROM Trainee t
                            JOIN t.trainers assigned
                            JOIN t.user tu
                            WHERE tu.username = :username
                        )
                        """, Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }

    public Trainee update(Trainee trainee) {
        return sessionFactory.getCurrentSession().merge(trainee);
    }

}
