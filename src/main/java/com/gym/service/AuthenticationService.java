package com.gym.service;

import com.gym.dao.UserDao;
import com.gym.exception.AuthenticationException;
import com.gym.model.User;
import com.gym.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {
    private final UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void login(final String username, final String password) {
        User user = userDao.findByUserName(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password");
        }

        SecurityContext.setCurrentUsername(username);
    }

    public void logout() {
        SecurityContext.clear();
    }

}
