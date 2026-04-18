package com.energy.kafka;

import com.energy.dto.Dtos.*;
import com.energy.model.*;
import com.energy.repository.*;
import com.energy.service.EnergyMapper;
import com.energy.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnergyKafkaConsumer {

    private final EnergyReadingRepository readingRepository;
    private final UserRepository          userRepository;
    private final AlertRepository         alertRepository;
    private final WebSocketNotificationService wsNotifier;
    private final EnergyMapper            mapper;

    @Value("${app.energy.alert-threshold-kwh:10.0}")
    private double alertThreshold;

    @KafkaListener(
        topics   = "${app.kafka.topic.energy-readings}",
        groupId  = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void consume(EnergyEvent event) {
        log.info("⚡ Consumed — user: {}, value: {} kWh", event.getUsername(), event.getConsumptionKwh());

        userRepository.findById(event.getUserId()).ifPresent(user -> {
            // 1. Persist
            EnergyReading reading = EnergyReading.builder()
                .user(user)
                .consumptionKwh(event.getConsumptionKwh())
                .timestamp(event.getTimestamp())
                .location(event.getLocation())
                .source(EnergyReading.ReadingSource.valueOf(event.getSource()))
                .build();
            readingRepository.save(reading);

            // 2. Push live reading via WebSocket
            wsNotifier.pushReading(user.getUsername(), mapper.toResponse(reading));

            // 3. Alert if above threshold
            if (event.getConsumptionKwh() > alertThreshold) {
                Alert alert = buildAlert(user, event.getConsumptionKwh());
                alertRepository.save(alert);
                wsNotifier.pushAlert(user.getUsername(), alert);
                log.warn("🚨 Alert [{}] for user {} — {} kWh",
                    alert.getSeverity(), user.getUsername(), event.getConsumptionKwh());
            }
        });
    }

    private Alert buildAlert(User user, double consumption) {
        Alert.Severity severity = consumption > 30 ? Alert.Severity.CRITICAL
            : consumption > 20 ? Alert.Severity.HIGH
            : consumption > 15 ? Alert.Severity.MEDIUM
            : Alert.Severity.LOW;

        return Alert.builder()
            .user(user)
            .message(String.format("High consumption: %.2f kWh (threshold: %.1f kWh)",
                consumption, alertThreshold))
            .severity(severity)
            .triggerValue(consumption)
            .threshold(alertThreshold)
            .acknowledged(false)
            .createdAt(LocalDateTime.now())
            .build();
    }
}
