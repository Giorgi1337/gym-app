package com.gym.service;

import com.gym.dto.LoginResponse;
import com.gym.exception.AuthenticationFailedException;
import com.gym.metrics.GymMetrics;
import com.gym.model.User;
import com.gym.repository.UserRepository;
import com.gym.security.BruteForceProtectionService;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountLockedException;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final GymMetrics gymMetrics;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository,
                                 GymMetrics gymMetrics,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService,
                                 BruteForceProtectionService bruteForceProtectionService,
                                 TokenBlacklistService tokenBlacklistService,
                                 PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.gymMetrics = gymMetrics;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.bruteForceProtectionService = bruteForceProtectionService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String username, String password)  {
        bruteForceProtectionService.checkNotBlocked(username);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            bruteForceProtectionService.registerFailure(username);
            gymMetrics.incrementLoginFailure();
            throw new AuthenticationFailedException("Invalid username or password");
        }

        bruteForceProtectionService.registerSuccess(username);
        gymMetrics.incrementLoginSuccess();

        String token = jwtService.generateToken(username);
        log.info("Authenticated user: {}", username);

        return new LoginResponse(token);
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Password changed for user: {}", username);
    }
}