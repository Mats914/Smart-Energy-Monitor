package com.energy.kafka;

import com.energy.dto.Dtos.EnergyEvent;
import com.energy.model.User;
import com.energy.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"energy-readings-test"})
@DirtiesContext
@DisplayName("Kafka consumer integration tests")
class EnergyKafkaConsumerTest {

    @Autowired private KafkaTemplate<String, EnergyEvent> kafkaTemplate;
    @Autowired private EnergyReadingRepository            readingRepo;
    @Autowired private UserRepository                     userRepo;
    @Autowired private AlertRepository                    alertRepo;
    @Autowired private PasswordEncoder                    encoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        alertRepo.deleteAll();
        readingRepo.deleteAll();
        userRepo.deleteAll();

        testUser = userRepo.save(User.builder()
            .username("kafkauser")
            .email("kafka@test.com")
            .password(encoder.encode("pass"))
            .role(User.Role.USER)
            .build());
    }

    @Test
    @DisplayName("Consumer — persists reading when event is consumed")
    void consumer_persistsReading() {
        EnergyEvent event = buildEvent(5.0);
        kafkaTemplate.send("energy-readings-test", event.getUserId().toString(), event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = readingRepo.countByUser(testUser);
            assertThat(count).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("Consumer — creates alert when consumption exceeds threshold")
    void consumer_createsAlertAboveThreshold() {
        EnergyEvent event = buildEvent(25.0);
        kafkaTemplate.send("energy-readings-test", event.getUserId().toString(), event);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            long alertCount = alertRepo.countByUserAndAcknowledgedFalse(testUser);
            assertThat(alertCount).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("Consumer — no alert when consumption is below threshold")
    void consumer_noAlertBelowThreshold() throws InterruptedException {
        EnergyEvent event = buildEvent(3.0);
        kafkaTemplate.send("energy-readings-test", event.getUserId().toString(), event);

        // Wait for reading to be persisted, then verify no alert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
            assertThat(readingRepo.countByUser(testUser)).isGreaterThan(0)
        );

        assertThat(alertRepo.countByUserAndAcknowledgedFalse(testUser)).isEqualTo(0);
    }

    private EnergyEvent buildEvent(double kwh) {
        EnergyEvent e = new EnergyEvent();
        e.setUserId(testUser.getId());
        e.setUsername(testUser.getUsername());
        e.setConsumptionKwh(kwh);
        e.setLocation("Test Meter");
        e.setSource("SIMULATED");
        e.setTimestamp(LocalDateTime.now());
        return e;
    }
}
