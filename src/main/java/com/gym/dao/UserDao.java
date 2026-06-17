package com.gym.dao;

import com.gym.model.User;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final SessionFactory sessionFactory;

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<String> findUsernamesByPrefix(String prefix) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                                SELECT u.username
                                FROM User u
                                WHERE u.username LIKE :prefix
                                """,
                        String.class
                )
                .setParameter("prefix", prefix + "%")
                .getResultList();
    }

    public Optional<User> findByUserName(String username) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                                SELECT u
                                FROM User u
                                WHERE u.username =:username
                                """,
                        User.class
                )
                .setParameter("username", username)
                .uniqueResultOptional();
    }

}
