package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.exception.*;
import com.energy.kafka.EnergyKafkaProducer;
import com.energy.model.*;
import com.energy.repository.*;
import com.energy.websocket.WebSocketNotificationService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnergyService unit tests")
class EnergyServiceTest {

    @Mock private EnergyReadingRepository readingRepo;
    @Mock private UserRepository          userRepo;
    @Mock private AlertRepository         alertRepo;
    @Mock private EnergyKafkaProducer     kafkaProducer;
    @Mock private WebSocketNotificationService wsNotifier;

    @InjectMocks private EnergyService service;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = User.builder()
            .id(1L).username("alice").email("alice@test.com")
            .password("hashed").role(User.Role.USER)
            .build();
    }

    // ── submitReading ─────────────────────────────────────────

    @Test
    @DisplayName("submitReading — publishes event to Kafka")
    void submitReading_publishesEvent() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));

        var req = new EnergyRequest();
        req.setConsumptionKwh(5.0);
        req.setLocation("Office");

        service.submitReading("alice", req);

        verify(kafkaProducer, times(1)).publishReading(argThat(e ->
            e.getUserId().equals(1L) &&
            e.getConsumptionKwh().equals(5.0) &&
            e.getLocation().equals("Office")
        ));
    }

    @Test
    @DisplayName("submitReading — defaults location to Main Meter when null")
    void submitReading_defaultsLocation() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));

        var req = new EnergyRequest();
        req.setConsumptionKwh(3.0);

        service.submitReading("alice", req);

        verify(kafkaProducer).publishReading(argThat(e -> e.getLocation().equals("Main Meter")));
    }

    @Test
    @DisplayName("submitReading — throws when user not found")
    void submitReading_userNotFound_throws() {
        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        var req = new EnergyRequest();
        req.setConsumptionKwh(5.0);

        assertThatThrownBy(() -> service.submitReading("ghost", req))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("ghost");
    }

    // ── getStats ──────────────────────────────────────────────

    @Test
    @DisplayName("getStats — returns correct rounded values")
    void getStats_returnsCorrectValues() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(readingRepo.sumConsumption(eq(alice), any(), any())).thenReturn(Optional.of(12.567));
        when(readingRepo.avgConsumption(alice)).thenReturn(Optional.of(6.123));
        when(readingRepo.maxConsumption(alice)).thenReturn(Optional.of(25.0));
        when(readingRepo.countByUser(alice)).thenReturn(42L);
        when(alertRepo.countByUserAndAcknowledgedFalse(alice)).thenReturn(3L);

        StatsResponse stats = service.getStats("alice");

        assertThat(stats.getTotalReadings()).isEqualTo(42L);
        assertThat(stats.getActiveAlerts()).isEqualTo(3L);
        assertThat(stats.getPeakConsumption()).isEqualTo(25.0);
        // Verify rounding
        assertThat(stats.getAverageDaily()).isEqualTo(6.12);
    }

    @Test
    @DisplayName("getStats — returns zeros when no data")
    void getStats_noData_returnsZeros() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(readingRepo.sumConsumption(any(), any(), any())).thenReturn(Optional.empty());
        when(readingRepo.avgConsumption(alice)).thenReturn(Optional.empty());
        when(readingRepo.maxConsumption(alice)).thenReturn(Optional.empty());
        when(readingRepo.countByUser(alice)).thenReturn(0L);
        when(alertRepo.countByUserAndAcknowledgedFalse(alice)).thenReturn(0L);

        StatsResponse stats = service.getStats("alice");

        assertThat(stats.getTotalToday()).isEqualTo(0.0);
        assertThat(stats.getPeakConsumption()).isEqualTo(0.0);
    }

    // ── deleteReading ─────────────────────────────────────────

    @Test
    @DisplayName("deleteReading — succeeds when user owns the reading")
    void deleteReading_ownerCanDelete() {
        EnergyReading reading = EnergyReading.builder()
            .id(10L).user(alice).consumptionKwh(5.0)
            .timestamp(LocalDateTime.now()).source(EnergyReading.ReadingSource.MANUAL)
            .build();

        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(readingRepo.findById(10L)).thenReturn(Optional.of(reading));

        assertThatCode(() -> service.deleteReading("alice", 10L)).doesNotThrowAnyException();
        verify(readingRepo).delete(reading);
    }

    @Test
    @DisplayName("deleteReading — throws UnauthorizedException for wrong owner")
    void deleteReading_wrongOwner_throws() {
        User bob = User.builder().id(2L).username("bob").build();
        EnergyReading reading = EnergyReading.builder()
            .id(10L).user(bob).consumptionKwh(5.0)
            .timestamp(LocalDateTime.now()).source(EnergyReading.ReadingSource.MANUAL)
            .build();

        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(readingRepo.findById(10L)).thenReturn(Optional.of(reading));

        assertThatThrownBy(() -> service.deleteReading("alice", 10L))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("deleteReading — throws ResourceNotFoundException for missing reading")
    void deleteReading_notFound_throws() {
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(readingRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteReading("alice", 99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ── acknowledgeAlert ──────────────────────────────────────

    @Test
    @DisplayName("acknowledgeAlert — sets acknowledged=true and pushes stats")
    void acknowledgeAlert_setsAcknowledged() {
        Alert alert = Alert.builder()
            .id(5L).user(alice).message("High consumption")
            .severity(Alert.Severity.HIGH).acknowledged(false)
            .createdAt(LocalDateTime.now()).build();

        when(alertRepo.findById(5L)).thenReturn(Optional.of(alert));
        when(userRepo.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(readingRepo.sumConsumption(any(), any(), any())).thenReturn(Optional.of(0.0));
        when(readingRepo.avgConsumption(any())).thenReturn(Optional.of(0.0));
        when(readingRepo.maxConsumption(any())).thenReturn(Optional.of(0.0));
        when(readingRepo.countByUser(any())).thenReturn(0L);
        when(alertRepo.countByUserAndAcknowledgedFalse(any())).thenReturn(0L);

        service.acknowledgeAlert("alice", 5L);

        assertThat(alert.isAcknowledged()).isTrue();
        verify(alertRepo).save(alert);
        verify(wsNotifier).pushStats(eq("alice"), any());
    }
}
