package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.exception.*;
import com.energy.kafka.EnergyKafkaProducer;
import com.energy.model.*;
import com.energy.repository.*;
import com.energy.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnergyService {

    private final EnergyReadingRepository readingRepository;
    private final UserRepository          userRepository;
    private final AlertRepository         alertRepository;
    private final EnergyKafkaProducer     kafkaProducer;
    private final WebSocketNotificationService wsNotifier;
    private final EnergyMapper            mapper;

    // ── Public API ────────────────────────────────────────────

    public void submitReading(String username, EnergyRequest request) {
        User user = findUser(username);
        EnergyEvent event = mapper.toEvent(user,
            request.getConsumptionKwh(),
            request.getLocation(),
            request.getSource() != null ? request.getSource().name() : null);
        kafkaProducer.publishReading(event);
    }

    public List<EnergyResponse> getReadings(String username) {
        User user = findUser(username);
        return readingRepository.findByUserOrderByTimestampDesc(user)
            .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    public List<EnergyResponse> getReadingsByPeriod(String username,
                                                     LocalDateTime start,
                                                     LocalDateTime end) {
        User user = findUser(username);
        return readingRepository
            .findByUserAndTimestampBetweenOrderByTimestampDesc(user, start, end)
            .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    public StatsResponse getStats(String username) {
        User user = findUser(username);
        LocalDateTime now = LocalDateTime.now();

        StatsResponse stats = new StatsResponse();
        stats.setTotalToday(round(
            readingRepository.sumConsumption(user, now.toLocalDate().atStartOfDay(), now).orElse(0.0)));
        stats.setTotalThisMonth(round(
            readingRepository.sumConsumption(user, now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now).orElse(0.0)));
        stats.setAverageDaily(round(
            readingRepository.avgConsumption(user).orElse(0.0)));
        stats.setPeakConsumption(round(
            readingRepository.maxConsumption(user).orElse(0.0)));
        stats.setTotalReadings(readingRepository.countByUser(user));
        stats.setActiveAlerts(alertRepository.countByUserAndAcknowledgedFalse(user));
        return stats;
    }

    @Transactional
    public void deleteReading(String username, Long id) {
        User user = findUser(username);
        EnergyReading reading = readingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reading not found: " + id));
        if (!reading.getUser().getId().equals(user.getId()))
            throw new UnauthorizedException("You do not own this reading");
        readingRepository.delete(reading);
    }

    public List<Alert> getAlerts(String username) {
        return alertRepository.findByUserOrderByCreatedAtDesc(findUser(username));
    }

    @Transactional
    public void acknowledgeAlert(String username, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
        alert.setAcknowledged(true);
        alertRepository.save(alert);
        wsNotifier.pushStats(username, getStats(username));
    }

    // ── Scheduler ─────────────────────────────────────────────

    @Transactional
    public void simulateAndPublish(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            double value = round(1.0 + Math.random() * 20.0);
            EnergyEvent event = mapper.toEvent(user, value, "Smart Meter", "SIMULATED");
            kafkaProducer.publishReading(event);
        });
    }

    // ── Helpers ───────────────────────────────────────────────

    private User findUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
