package com.energy.kafka;

import com.energy.config.AppProperties;
import com.energy.dto.Dtos.EnergyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnergyKafkaProducer {

    private final KafkaTemplate<String, EnergyEvent> kafkaTemplate;
    private final AppProperties props;

    public void publishReading(EnergyEvent event) {
        String topic = props.getKafka().getTopic().getEnergyReadings();
        kafkaTemplate.send(topic, event.getUserId().toString(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish reading for user {}: {}", event.getUserId(), ex.getMessage());
                } else {
                    log.debug("Published reading for user {} to partition {}",
                        event.getUserId(),
                        result.getRecordMetadata().partition());
                }
            });
    }
}
