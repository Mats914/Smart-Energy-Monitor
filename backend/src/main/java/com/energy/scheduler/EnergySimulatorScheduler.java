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

    private final EnergyService   energyService;
    private final UserRepository  userRepository;

    /**
     * Every 30 seconds: publish a simulated reading for every user via Kafka.
     * Kafka Consumer handles persistence + alerts + WebSocket push.
     */
    @Scheduled(fixedRateString = "${app.energy.simulation-interval-ms}")
    public void simulateReadings() {
        var users = userRepository.findAll();
        log.info("⚡ Scheduler: publishing {} simulated readings via Kafka", users.size());
        users.forEach(u -> energyService.simulateAndPublish(u.getId()));
    }
}
