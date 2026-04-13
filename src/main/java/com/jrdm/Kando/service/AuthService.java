package com.jrdm.Kando.service;

import com.jrdm.Kando.config.security.JwtService;
import com.jrdm.Kando.domain.model.User;
import com.jrdm.Kando.repository.UserRepository;
import com.jrdm.Kando.service.dto.AuthResponse;
import com.jrdm.Kando.service.dto.LoginRequest;
import com.jrdm.Kando.service.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.create(
                req.getEmail(),
                passwordEncoder.encode(req.getPassword()),
                req.getDisplayName()
        );
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), List.of("ROLE_USER"));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), List.of("ROLE_USER"));

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .build();
    }
}
