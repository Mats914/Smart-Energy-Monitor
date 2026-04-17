package com.energy.controller;

import com.energy.dto.Dtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"energy-readings-test", "energy-alerts-test"})
@DirtiesContext
@DisplayName("AuthController integration tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc      mockMvc;
    @Autowired private ObjectMapper mapper;

    @Test
    @DisplayName("POST /api/auth/register — returns token on success")
    void register_success() throws Exception {
        var req = new RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@test.com");
        req.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.username").value("newuser"))
            .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register — 400 for invalid email")
    void register_invalidEmail_returns400() throws Exception {
        var req = new RegisterRequest();
        req.setUsername("user");
        req.setEmail("not-an-email");
        req.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message.email").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register — 400 for short password")
    void register_shortPassword_returns400() throws Exception {
        var req = new RegisterRequest();
        req.setUsername("user");
        req.setEmail("user@test.com");
        req.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login — 401 for wrong password")
    void login_wrongPassword_returns401() throws Exception {
        var req = new LoginRequest();
        req.setUsername("nobody");
        req.setPassword("wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }
}
