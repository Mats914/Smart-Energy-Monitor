package com.energy.controller;

import com.energy.dto.Dtos.*;
import com.energy.model.User;
import com.energy.repository.UserRepository;
import com.energy.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"energy-readings-test", "energy-alerts-test"})
@DirtiesContext
@DisplayName("EnergyController integration tests")
class EnergyControllerIntegrationTest {

    @Autowired private MockMvc        mockMvc;
    @Autowired private ObjectMapper   mapper;
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtil        jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        User user = User.builder()
            .username("testuser")
            .email("test@test.com")
            .password(encoder.encode("password"))
            .role(User.Role.USER)
            .build();
        userRepo.save(user);
        token = jwtUtil.generateToken("testuser");
    }

    @Test
    @DisplayName("POST /api/energy — accepted with valid token")
    void submitReading_accepted() throws Exception {
        var req = new EnergyRequest();
        req.setConsumptionKwh(7.5);
        req.setLocation("Office");

        mockMvc.perform(post("/api/energy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.message").value("Reading submitted for processing"));
    }

    @Test
    @DisplayName("POST /api/energy — 400 when consumptionKwh is missing")
    void submitReading_missingValue_returns400() throws Exception {
        mockMvc.perform(post("/api/energy")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /api/energy — 401 without token")
    void getReadings_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/energy"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/energy/stats — returns stats object")
    void getStats_returnsStats() throws Exception {
        mockMvc.perform(get("/api/energy/stats")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalToday").exists())
            .andExpect(jsonPath("$.totalReadings").exists());
    }

    @Test
    @DisplayName("GET /api/energy/alerts — returns list")
    void getAlerts_returnsList() throws Exception {
        mockMvc.perform(get("/api/energy/alerts")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("DELETE /api/energy/999 — 404 for non-existent reading")
    void deleteReading_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/energy/999")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isNotFound());
    }
}
