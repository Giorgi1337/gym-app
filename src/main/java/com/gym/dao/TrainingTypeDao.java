package com.gym.dao;

import com.gym.model.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TrainingTypeDao {

    private final SessionFactory sessionFactory;

    public TrainingTypeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public TrainingType findByName(final String name) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                                SELECT t 
                                FROM TrainingType t 
                                WHERE LOWER(t.trainingTypeName) = LOWER(:name) 
                                """,
                        TrainingType.class
                )
                .setParameter("name", name)
                .getSingleResultOrNull();

    }
}
