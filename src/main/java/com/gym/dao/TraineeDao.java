package com.gym.dao;

import com.gym.model.Trainee;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TraineeDao {

    private final SessionFactory sessionFactory;

    public TraineeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainee trainee) {
        sessionFactory.getCurrentSession().persist(trainee);
    }

}
