package com.energy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Energy energy = new Energy();
    private Kafka kafka = new Kafka();

    @Data
    public static class Jwt {
        private String secret;
        private long expiration;
    }

    @Data
    public static class Energy {
        private double alertThresholdKwh = 10.0;
        private long simulationIntervalMs = 30000;
    }

    @Data
    public static class Kafka {
        private Topic topic = new Topic();

        @Data
        public static class Topic {
            private String energyReadings;
            private String alerts;
        }
    }
}
