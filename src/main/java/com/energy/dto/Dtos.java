package com.energy.dto;

import com.energy.model.EnergyReading;
import com.energy.model.User;
import lombok.Data;
import java.time.LocalDateTime;

public class Dtos {

    // ---- AUTH ----
    @Data
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String username;
        private String role;

        public AuthResponse(String token, String username, User.Role role) {
            this.token = token;
            this.username = username;
            this.role = role.name();
        }
    }

    // ---- ENERGY ----
    @Data
    public static class EnergyRequest {
        private Double consumptionKwh;
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

    // ---- STATS ----
    @Data
    public static class StatsResponse {
        private Double totalToday;
        private Double totalThisMonth;
        private Double averageDaily;
        private Double peakConsumption;
        private Long totalReadings;
    }
}
