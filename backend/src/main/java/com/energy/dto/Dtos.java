package com.energy.dto;

import com.energy.model.Alert;
import com.energy.model.EnergyReading;
import com.energy.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

public final class Dtos {

    private Dtos() {}

    // ── Auth ──────────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        @NotBlank @Size(min = 3, max = 50)
        private String username;

        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6)
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank private String username;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private final String token;
        private final String username;
        private final String role;

        public AuthResponse(String token, String username, User.Role role) {
            this.token    = token;
            this.username = username;
            this.role     = role.name();
        }
    }

    // ── Energy ────────────────────────────────────────────────

    @Data
    public static class EnergyRequest {
        @NotNull @DecimalMin("0.1") @DecimalMax("1000.0")
        private Double consumptionKwh;

        @Size(max = 100)
        private String location;

        private EnergyReading.ReadingSource source;
    }

    @Data
    public static class EnergyResponse {
        private Long id;
        private Double consumptionKwh;
        private LocalDateTime timestamp;
        private String location;
        private String source;
        private String username;
    }

    // ── Stats ─────────────────────────────────────────────────

    @Data
    public static class StatsResponse {
        private Double totalToday;
        private Double totalThisMonth;
        private Double averageDaily;
        private Double peakConsumption;
        private Long   totalReadings;
        private Long   activeAlerts;
    }

    // ── Kafka event (also used for WebSocket push) ────────────

    @Data
    public static class EnergyEvent {
        private Long   userId;
        private String username;
        private Double consumptionKwh;
        private String location;
        private String source;
        private LocalDateTime timestamp;
    }

    // ── WebSocket live update ─────────────────────────────────

    @Data
    public static class LiveUpdate {
        private String type;      // "READING" | "ALERT"
        private Object payload;
    }
}
