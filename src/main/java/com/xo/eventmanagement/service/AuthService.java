package com.xo.eventmanagement.service;

import com.xo.eventmanagement.dto.ApiResponse;
import com.xo.eventmanagement.dto.JwtAuthResponse;
import com.xo.eventmanagement.dto.LoginRequest;
import com.xo.eventmanagement.dto.SignupRequest;
import com.xo.eventmanagement.entity.User;
import com.xo.eventmanagement.exception.BadRequestException;
import com.xo.eventmanagement.exception.ResourceNotFoundException;
import com.xo.eventmanagement.repository.UserRepository;
import com.xo.eventmanagement.security.JwtTokenProvider;
import com.xo.eventmanagement.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    public JwtAuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in. Check your inbox for the verification code, or request a new one.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String role = userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return new JwtAuthResponse(jwt, userPrincipal.getId(), userPrincipal.getName(),
                userPrincipal.getUsername(), role);
    }

    public User register(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email is already registered. Please use a different email or login.");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setPhone(signupRequest.getPhone());

        if ("ORGANIZER".equalsIgnoreCase(signupRequest.getRole())) {
            user.setRole(User.Role.ORGANIZER);
        } else {
            user.setRole(User.Role.ATTENDEE);
        }

        user.setEmailVerified(false);
        generateAndAssignCode(user);

        User saved = userRepository.save(user);
        emailService.sendVerificationCode(saved.getEmail(), saved.getName(), saved.getVerificationCode());
        return saved;
    }

    public ApiResponse verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this email"));

        if (user.isEmailVerified()) {
            return new ApiResponse(true, "Email is already verified. You can log in now.");
        }

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(code)) {
            throw new BadRequestException("Invalid verification code. Please check and try again.");
        }

        if (user.getVerificationCodeExpiry() == null || user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This code has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        return new ApiResponse(true, "Email verified successfully! You can now log in.");
    }

    public ApiResponse resendCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this email"));

        if (user.isEmailVerified()) {
            return new ApiResponse(true, "This email is already verified. You can log in now.");
        }

        generateAndAssignCode(user);
        userRepository.save(user);
        emailService.sendVerificationCode(user.getEmail(), user.getName(), user.getVerificationCode());

        return new ApiResponse(true, "A new verification code has been sent to your email.");
    }

    private void generateAndAssignCode(User user) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        user.setVerificationCode(code);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
    }
}
