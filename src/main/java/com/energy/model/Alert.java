package com.energy.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    private Double triggerValue;
    private Double threshold;
    private boolean acknowledged;
    private LocalDateTime createdAt;

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
