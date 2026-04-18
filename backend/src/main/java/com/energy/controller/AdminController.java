package com.energy.controller;

import com.energy.model.AuditLog;
import com.energy.model.User;
import com.energy.repository.AuditLogRepository;
import com.energy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository    userRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    @GetMapping("/audit")
    public ResponseEntity<Page<AuditLog>> getAuditLog(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(
            auditLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size))
        );
    }

    @GetMapping("/audit/{username}")
    public ResponseEntity<List<AuditLog>> getUserAuditLog(@PathVariable String username) {
        return ResponseEntity.ok(auditLogRepository.findByUsernameOrderByTimestampDesc(username));
    }
}
