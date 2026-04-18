package com.energy.service;

import com.energy.dto.Dtos.*;
import com.energy.model.*;
import org.springframework.stereotype.Component;

/**
 * Central mapper — eliminates duplicated toResponse() methods
 * across EnergyService and EnergyKafkaConsumer.
 */
@Component
public class EnergyMapper {

    public EnergyResponse toResponse(EnergyReading r) {
        EnergyResponse res = new EnergyResponse();
        res.setId(r.getId());
        res.setConsumptionKwh(r.getConsumptionKwh());
        res.setTimestamp(r.getTimestamp());
        res.setLocation(r.getLocation());
        res.setSource(r.getSource().name());
        res.setUsername(r.getUser().getUsername());
        return res;
    }

    public EnergyEvent toEvent(User user, double kwh, String location, String source) {
        EnergyEvent event = new EnergyEvent();
        event.setUserId(user.getId());
        event.setUsername(user.getUsername());
        event.setConsumptionKwh(round(kwh));
        event.setLocation(location != null ? location : "Main Meter");
        event.setSource(source != null ? source : "MANUAL");
        event.setTimestamp(java.time.LocalDateTime.now());
        return event;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
