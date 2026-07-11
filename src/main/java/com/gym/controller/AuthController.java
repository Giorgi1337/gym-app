package com.gym.controller;

import com.gym.api.AuthApi;
import com.gym.dto.AuthRequest;
import com.gym.dto.ChangePasswordRequest;
import com.gym.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController implements AuthApi {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public ResponseEntity<Void> login(AuthRequest request) {
        authenticationService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> changePassword(String username, ChangePasswordRequest request) {
        authenticationService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}