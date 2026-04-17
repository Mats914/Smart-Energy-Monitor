package com.energy.controller;

import com.energy.dto.Dtos.*;
import com.energy.model.Alert;
import com.energy.service.EnergyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
public class EnergyController {

    private final EnergyService energyService;

    @PostMapping
    public ResponseEntity<Map<String, String>> submit(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody EnergyRequest request) {
        energyService.submitReading(user.getUsername(), request);
        return ResponseEntity.accepted().body(Map.of("message", "Reading submitted for processing"));
    }

    @GetMapping
    public ResponseEntity<List<EnergyResponse>> getReadings(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(energyService.getReadings(user.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(energyService.getStats(user.getUsername()));
    }

    @GetMapping("/range")
    public ResponseEntity<List<EnergyResponse>> getRange(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(energyService.getReadingsByPeriod(user.getUsername(), start, end));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        energyService.deleteReading(user.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Reading deleted"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(energyService.getAlerts(user.getUsername()));
    }

    @PutMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<Map<String, String>> acknowledge(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long id) {
        energyService.acknowledgeAlert(user.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Alert acknowledged"));
    }
}
