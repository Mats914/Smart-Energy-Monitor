package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.exception.ConflictException;
import com.energy.model.User;
import com.energy.repository.UserRepository;
import com.energy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username already taken");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered");

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(User.Role.USER)
            .build();

        userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(user.getUsername()), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernamePasswordAuthenticationToken("", "").getClass().cast(null));
        return new AuthResponse(jwtUtil.generateToken(user.getUsername()), user.getUsername(), user.getRole());
    }
}
