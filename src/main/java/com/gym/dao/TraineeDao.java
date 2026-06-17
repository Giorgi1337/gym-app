package com.gym.dao;

import com.gym.model.Trainee;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

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

}
