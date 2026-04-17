package com.energy.websocket;

import com.energy.dto.Dtos.*;
import com.energy.model.Alert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push a new reading to all subscribers of /topic/readings/{username}
     */
    public void pushReading(String username, EnergyResponse reading) {
        LiveUpdate update = new LiveUpdate();
        update.setType("READING");
        update.setPayload(reading);

        messagingTemplate.convertAndSend("/topic/readings/" + username, update);
        log.debug("📡 Pushed reading to WebSocket for user: {}", username);
    }

    /**
     * Push an alert to /topic/alerts/{username}
     */
    public void pushAlert(String username, Alert alert) {
        LiveUpdate update = new LiveUpdate();
        update.setType("ALERT");
        update.setPayload(alert);

        messagingTemplate.convertAndSend("/topic/alerts/" + username, update);
        log.debug("📡 Pushed alert to WebSocket for user: {}", username);
    }

    /**
     * Push stats update to /topic/stats/{username}
     */
    public void pushStats(String username, StatsResponse stats) {
        LiveUpdate update = new LiveUpdate();
        update.setType("STATS");
        update.setPayload(stats);

        messagingTemplate.convertAndSend("/topic/stats/" + username, update);
    }
}
