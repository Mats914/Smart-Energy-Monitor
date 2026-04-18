package com.energy.service;

import com.energy.model.AuditLog;
import com.energy.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditRepo;

    /**
     * Fire-and-forget — runs in background thread pool.
     * Never blocks the main request.
     */
    @Async
    public void log(String username, String action, String detail) {
        log(username, action, detail, null);
    }

    @Async
    public void log(String username, String action, String detail, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                .username(username)
                .action(action)
                .detail(detail)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();
            auditRepo.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to persist audit log — user: {}, action: {}", username, action);
        }
    }
}
