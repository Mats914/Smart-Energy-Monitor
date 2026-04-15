package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.model.*;
import com.energy.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnergyServiceTest {

    @Mock private EnergyReadingRepository energyRepo;
    @Mock private UserRepository userRepo;
    @Mock private AlertRepository alertRepo;

    @InjectMocks private EnergyService energyService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded")
                .role(User.Role.USER)
                .build();
    }

    @Test
    void addReading_shouldSaveAndReturnResponse() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        EnergyReading savedReading = EnergyReading.builder()
                .id(1L)
                .user(testUser)
                .consumptionKwh(5.0)
                .timestamp(LocalDateTime.now())
                .location("Main Meter")
                .source(EnergyReading.ReadingSource.MANUAL)
                .build();

        when(energyRepo.save(any(EnergyReading.class))).thenReturn(savedReading);

        EnergyRequest request = new EnergyRequest();
        request.setConsumptionKwh(5.0);
        request.setLocation("Main Meter");

        EnergyResponse response = energyService.addReading("testuser", request);

        assertThat(response).isNotNull();
        assertThat(response.getConsumptionKwh()).isEqualTo(5.0);
        verify(energyRepo, times(1)).save(any(EnergyReading.class));
    }

    @Test
    void addReading_highConsumption_shouldCreateAlert() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        EnergyReading savedReading = EnergyReading.builder()
                .id(1L).user(testUser)
                .consumptionKwh(25.0)
                .timestamp(LocalDateTime.now())
                .location("Main Meter")
                .source(EnergyReading.ReadingSource.MANUAL)
                .build();

        when(energyRepo.save(any(EnergyReading.class))).thenReturn(savedReading);

        EnergyRequest request = new EnergyRequest();
        request.setConsumptionKwh(25.0); // Above 10.0 threshold

        energyService.addReading("testuser", request);

        verify(alertRepo, times(1)).save(any(Alert.class));
    }

    @Test
    void getStats_shouldReturnCorrectValues() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(energyRepo.sumConsumptionByUserAndPeriod(eq(testUser), any(), any()))
                .thenReturn(Optional.of(12.5));
        when(energyRepo.avgConsumptionByUser(testUser)).thenReturn(Optional.of(6.25));
        when(energyRepo.maxConsumptionByUser(testUser)).thenReturn(Optional.of(15.0));
        when(energyRepo.countByUser(testUser)).thenReturn(10L);

        StatsResponse stats = energyService.getStats("testuser");

        assertThat(stats.getTotalReadings()).isEqualTo(10L);
        assertThat(stats.getAverageDaily()).isEqualTo(6.25);
        assertThat(stats.getPeakConsumption()).isEqualTo(15.0);
    }

    @Test
    void deleteReading_wrongUser_shouldThrowException() {
        User otherUser = User.builder().id(2L).username("other").build();

        EnergyReading reading = EnergyReading.builder()
                .id(1L)
                .user(otherUser)
                .build();

        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(energyRepo.findById(1L)).thenReturn(Optional.of(reading));

        assertThatThrownBy(() -> energyService.deleteReading("testuser", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not authorized");
    }
}
