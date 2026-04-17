package com.energy.kafka;

import com.energy.dto.Dtos.*;
import com.energy.model.*;
import com.energy.repository.*;
import com.energy.websocket.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnergyKafkaConsumer {

    private final EnergyReadingRepository readingRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final WebSocketNotificationService wsNotifier;

    private static final double ALERT_THRESHOLD = 10.0;

    @KafkaListener(topics = "${app.kafka.topic.energy-readings}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consume(EnergyEvent event) {
        log.info("⚡ Consumed event — user: {}, value: {} kWh", event.getUsername(), event.getConsumptionKwh());

        // 1. Persist the reading
        userRepository.findById(event.getUserId()).ifPresent(user -> {
            EnergyReading reading = EnergyReading.builder()
                .user(user)
                .consumptionKwh(event.getConsumptionKwh())
                .timestamp(event.getTimestamp())
                .location(event.getLocation())
                .source(EnergyReading.ReadingSource.valueOf(event.getSource()))
                .build();
            readingRepository.save(reading);

            // 2. Push live update to WebSocket subscribers
            wsNotifier.pushReading(user.getUsername(), toResponse(reading));

            // 3. Check threshold and create alert if needed
            if (event.getConsumptionKwh() > ALERT_THRESHOLD) {
                Alert alert = buildAlert(user, event.getConsumptionKwh());
                alertRepository.save(alert);
                wsNotifier.pushAlert(user.getUsername(), alert);
                log.warn("🚨 Alert created for user {} — {} kWh", user.getUsername(), event.getConsumptionKwh());
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
            .message(String.format("High consumption detected: %.2f kWh (threshold: %.1f kWh)",
                consumption, ALERT_THRESHOLD))
            .severity(severity)
            .triggerValue(consumption)
            .threshold(ALERT_THRESHOLD)
            .acknowledged(false)
            .createdAt(LocalDateTime.now())
            .build();
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
}
