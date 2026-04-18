package com.energy.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_time", columnList = "username,timestamp")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String action;        // e.g. "SUBMIT_READING", "LOGIN", "DELETE_READING"

    @Column(length = 255)
    private String detail;        // extra context

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 45)
    private String ipAddress;
}
