package com.energy.scheduler;

import com.energy.repository.UserRepository;
import com.energy.service.EnergyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnergySimulatorScheduler {

    private final EnergyService energyService;
    private final UserRepository userRepository;

    /**
     * Simulates energy readings every 30 seconds for all users.
     * In production this would be replaced by actual sensor data / Kafka consumers.
     */
    @Scheduled(fixedRate = 30000) // every 30 seconds
    public void simulateEnergyReadings() {
        log.info("⚡ Simulating energy readings...");
        userRepository.findAll().forEach(user -> {
            energyService.simulateReading(user.getId());
            log.info("  → Generated reading for user: {}", user.getUsername());
        });
    }
}
