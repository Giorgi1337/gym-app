package com.gym.service;

import com.gym.exception.AuthenticationFailedException;
import com.gym.metrics.GymMetrics;
import com.gym.model.User;
import com.gym.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final GymMetrics gymMetrics;

    public AuthenticationService(UserRepository userRepository, GymMetrics gymMetrics) {
        this.userRepository = userRepository;
        this.gymMetrics = gymMetrics;
    }

    public void login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    gymMetrics.incrementLoginFailure();
                    return new AuthenticationFailedException("Invalid username or password");
                });

        if (!user.getPassword().equals(password)) {
            gymMetrics.incrementLoginFailure();
            throw new AuthenticationFailedException("Invalid username or password");
        }

        gymMetrics.incrementLoginSuccess();
        log.info("Authenticated user: {}", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!user.getPassword().equals(oldPassword)) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        user.setPassword(newPassword);
        log.info("Password changed for user: {}", username);
    }
}