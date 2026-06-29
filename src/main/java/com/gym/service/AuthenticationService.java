package com.gym.service;

import com.gym.dao.UserDao;
import com.gym.exception.AuthenticationFailedException;
import com.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void login(String username, String password) {
        User user = userDao.findByUserName(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        log.info("Authenticated user: {}", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userDao.findByUserName(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!user.getPassword().equals(oldPassword)) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        user.setPassword(newPassword);
        log.info("Password changed for user: {}", username);
    }
}