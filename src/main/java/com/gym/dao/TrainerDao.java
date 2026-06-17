package com.gym.dao;

import com.gym.model.Trainer;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TrainerDao {

    private final SessionFactory sessionFactory;

    public TrainerDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(Trainer trainer) {
        sessionFactory.getCurrentSession().persist(trainer);
    }


}
