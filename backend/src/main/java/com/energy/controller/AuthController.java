package com.energy.controller;

import com.energy.dto.Dtos.*;
import com.energy.service.AuditService;
import com.energy.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService  authService;
    private final AuditService auditService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                  HttpServletRequest httpRequest) {
        AuthResponse res = authService.register(request);
        auditService.log(request.getUsername(), "REGISTER", "New account created", httpRequest.getRemoteAddr());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        AuthResponse res = authService.login(request);
        auditService.log(request.getUsername(), "LOGIN", "Successful login", httpRequest.getRemoteAddr());
        return ResponseEntity.ok(res);
    }
}
