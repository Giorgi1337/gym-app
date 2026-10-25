package com.gym.controller;

import com.gym.api.AuthApi;
import com.gym.dto.AuthRequest;
import com.gym.dto.ChangePasswordRequest;
import com.gym.dto.LoginResponse;
import com.gym.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public ResponseEntity<LoginResponse> login(AuthRequest request)  {
        LoginResponse response = authenticationService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Void> logout(String authHeader) {
        authenticationService.logout(authHeader.replace("Bearer ", ""));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#username == authentication.name")
    public ResponseEntity<Void> changePassword(String username, ChangePasswordRequest request) {
        authenticationService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}