package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.model.*;
import com.energy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnergyService {

    private final EnergyReadingRepository energyRepo;
    private final UserRepository userRepo;
    private final AlertRepository alertRepo;

    private static final double ALERT_THRESHOLD_KWH = 10.0;

    public EnergyResponse addReading(String username, EnergyRequest request) {
        User user = getUser(username);

        EnergyReading reading = EnergyReading.builder()
                .user(user)
                .consumptionKwh(request.getConsumptionKwh())
                .timestamp(LocalDateTime.now())
                .location(request.getLocation() != null ? request.getLocation() : "Main Meter")
                .source(request.getSource() != null ? request.getSource() : EnergyReading.ReadingSource.MANUAL)
                .build();

        energyRepo.save(reading);

        // Check and create alert if needed
        checkAndCreateAlert(user, request.getConsumptionKwh());

        return toResponse(reading);
    }

    public List<EnergyResponse> getReadings(String username) {
        User user = getUser(username);
        return energyRepo.findByUserOrderByTimestampDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EnergyResponse> getReadingsByPeriod(String username, LocalDateTime start, LocalDateTime end) {
        User user = getUser(username);
        return energyRepo.findByUserAndTimestampBetweenOrderByTimestampDesc(user, start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public StatsResponse getStats(String username) {
        User user = getUser(username);
        LocalDateTime now = LocalDateTime.now();

        Double todayTotal = energyRepo.sumConsumptionByUserAndPeriod(
                user, now.toLocalDate().atStartOfDay(), now).orElse(0.0);

        Double monthTotal = energyRepo.sumConsumptionByUserAndPeriod(
                user, now.withDayOfMonth(1).toLocalDate().atStartOfDay(), now).orElse(0.0);

        Double avgDaily = energyRepo.avgConsumptionByUser(user).orElse(0.0);
        Double peak = energyRepo.maxConsumptionByUser(user).orElse(0.0);
        long totalReadings = energyRepo.countByUser(user);

        StatsResponse stats = new StatsResponse();
        stats.setTotalToday(round(todayTotal));
        stats.setTotalThisMonth(round(monthTotal));
        stats.setAverageDaily(round(avgDaily));
        stats.setPeakConsumption(round(peak));
        stats.setTotalReadings(totalReadings);
        return stats;
    }

    public void deleteReading(String username, Long id) {
        User user = getUser(username);
        EnergyReading reading = energyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reading not found"));
        if (!reading.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Not authorized");
        }
        energyRepo.delete(reading);
    }

    private void checkAndCreateAlert(User user, double consumption) {
        if (consumption > ALERT_THRESHOLD_KWH) {
            Alert.AlertSeverity severity;
            if (consumption > 30) severity = Alert.AlertSeverity.CRITICAL;
            else if (consumption > 20) severity = Alert.AlertSeverity.HIGH;
            else if (consumption > 15) severity = Alert.AlertSeverity.MEDIUM;
            else severity = Alert.AlertSeverity.LOW;

            Alert alert = Alert.builder()
                    .user(user)
                    .message(String.format("High energy consumption detected: %.2f kWh (threshold: %.1f kWh)",
                            consumption, ALERT_THRESHOLD_KWH))
                    .severity(severity)
                    .triggerValue(consumption)
                    .threshold(ALERT_THRESHOLD_KWH)
                    .acknowledged(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            alertRepo.save(alert);
        }
    }

    public List<Alert> getAlerts(String username) {
        User user = getUser(username);
        return alertRepo.findByUserOrderByCreatedAtDesc(user);
    }

    public void acknowledgeAlert(String username, Long alertId) {
        Alert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setAcknowledged(true);
        alertRepo.save(alert);
    }

    // Called by scheduler
    public void simulateReading(Long userId) {
        userRepo.findById(userId).ifPresent(user -> {
            double simulated = 1.0 + Math.random() * 15.0; // 1-16 kWh
            EnergyReading reading = EnergyReading.builder()
                    .user(user)
                    .consumptionKwh(round(simulated))
                    .timestamp(LocalDateTime.now())
                    .location("Smart Meter")
                    .source(EnergyReading.ReadingSource.SIMULATED)
                    .build();
            energyRepo.save(reading);
            checkAndCreateAlert(user, simulated);
        });
    }

    private EnergyResponse toResponse(EnergyReading r) {
        EnergyResponse res = new EnergyResponse();
        res.setId(r.getId());
        res.setConsumptionKwh(r.getConsumptionKwh());
        res.setTimestamp(r.getTimestamp());
        res.setLocation(r.getLocation());
        res.setSource(r.getSource().name());
        res.setUsername(r.getUser().getUsername());
        return res;
    }

    private User getUser(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
