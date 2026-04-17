package com.energy.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.energy-readings}")
    private String energyReadingsTopic;

    @Value("${app.kafka.topic.alerts}")
    private String alertsTopic;

    @Bean
    public NewTopic energyReadingsTopic() {
        return TopicBuilder.name(energyReadingsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic alertsTopic() {
        return TopicBuilder.name(alertsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
