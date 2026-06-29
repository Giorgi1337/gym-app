package com.gym.dao;

import com.gym.model.TrainingType;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    public TrainingType findById(final Long id) {
        return sessionFactory.getCurrentSession().find(TrainingType.class, id);
    }

    public List<TrainingType> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT t FROM TrainingType t ORDER BY t.trainingTypeName", TrainingType.class)
                .getResultList();
    }

}
