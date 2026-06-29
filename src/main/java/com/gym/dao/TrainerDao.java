package com.gym.dao;

import com.gym.model.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TrainerDao {

    private final SessionFactory sessionFactory;

    public TrainerDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
    }

    public Trainer update(Trainer trainer) {
        return sessionFactory.getCurrentSession().merge(trainer);
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

    public List<Trainer> findByUsernames(Set<String> usernames) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                            SELECT t
                            FROM Trainer t
                            JOIN FETCH t.user u
                            WHERE u.username IN :usernames
                            """,
                        Trainer.class
                )
                .setParameter("usernames", usernames)
                .getResultList();
    }

}
