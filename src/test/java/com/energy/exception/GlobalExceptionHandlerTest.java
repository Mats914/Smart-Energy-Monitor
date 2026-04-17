package com.energy.exception;

import com.energy.controller.EnergyController;
import com.energy.service.EnergyService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnergyController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
@DisplayName("GlobalExceptionHandler tests")
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc   mockMvc;
    @MockBean  private EnergyService energyService;

    @Test
    @WithMockUser(username = "alice")
    @DisplayName("ResourceNotFoundException → 404 with error body")
    void resourceNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Reading not found: 99"))
            .when(energyService).deleteReading("alice", 99L);

        mockMvc.perform(delete("/api/energy/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Reading not found: 99"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser(username = "alice")
    @DisplayName("UnauthorizedException → 403 with error body")
    void unauthorized_returns403() throws Exception {
        doThrow(new UnauthorizedException("You do not own this reading"))
            .when(energyService).deleteReading("alice", 5L);

        mockMvc.perform(delete("/api/energy/5"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }
}
