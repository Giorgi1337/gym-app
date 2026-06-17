package com.gym.dao;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    private final SessionFactory sessionFactory;

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<String> findUsernamesByPrefix(String prefix) {
        return sessionFactory.getCurrentSession()
                .createQuery(
                        """
                                SELECT u.username
                                FROM User u
                                WHERE u.username LIKE :prefix
                                """,
                        String.class
                )
                .setParameter("prefix", prefix + "%")
                .getResultList();
    }

}
