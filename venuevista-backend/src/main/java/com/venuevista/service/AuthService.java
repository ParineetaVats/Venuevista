package com.venuevista.service;

import com.venuevista.dto.AuthResponse;
import com.venuevista.dto.LoginRequest;
import com.venuevista.dto.RegisterRequest;
import com.venuevista.dto.UserResponse;
import com.venuevista.entity.Role;
import com.venuevista.entity.User;
import com.venuevista.exception.EmailExistsException;
import com.venuevista.repository.UserRepository;
import com.venuevista.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new EmailExistsException("Email is already registered: " + request.email());
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(cleanEmail);
        user.setPhone(request.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setCity(request.city() != null ? request.city().trim() : null);
        user.setInterest(request.interest() != null ? request.interest().trim() : null);
        user.setGuests(request.guests());
        user.setBudget(request.budget());

        User savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        Instant expiresAt = Instant.now().plus(1440, ChronoUnit.MINUTES);

        return new AuthResponse(token, expiresAt, toUserResponse(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanEmail, request.password())
        );

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        Instant expiresAt = Instant.now().plus(1440, ChronoUnit.MINUTES);

        return new AuthResponse(token, expiresAt, toUserResponse(user));
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCity(),
                user.getInterest(),
                user.getGuests(),
                user.getBudget()
        );
    }
}
