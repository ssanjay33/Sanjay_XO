package com.xo.eventmanagement.controller;

import com.xo.eventmanagement.dto.*;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        User user = authService.register(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Account created! We've sent a 6-digit verification code to " + user.getEmail() + ". Please verify to activate your account."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request.getEmail(), request.getCode()));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse> resendCode(@Valid @RequestBody ResendCodeRequest request) {
        return ResponseEntity.ok(authService.resendCode(request.getEmail()));
    }
}
