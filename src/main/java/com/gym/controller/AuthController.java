package com.gym.controller;

import com.gym.dto.AuthRequest;
import com.gym.dto.ChangePasswordRequest;
import com.gym.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication API")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Login", description = "Validates that username and password match")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody AuthRequest request) {
        authenticationService.login(request.username(), request.password());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password", description = "Changes password after validating the old password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid username or old password")
    })
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody ChangePasswordRequest request) {

        authenticationService.changePassword(username, request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}