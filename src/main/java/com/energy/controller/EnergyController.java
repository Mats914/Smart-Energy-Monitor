package com.energy.controller;

import com.energy.dto.Dtos.*;
import com.energy.model.Alert;
import com.energy.service.EnergyService;
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
    public ResponseEntity<EnergyResponse> addReading(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody EnergyRequest request) {
        return ResponseEntity.ok(energyService.addReading(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<EnergyResponse>> getMyReadings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(energyService.getReadings(userDetails.getUsername()));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(energyService.getStats(userDetails.getUsername()));
    }

    @GetMapping("/range")
    public ResponseEntity<List<EnergyResponse>> getByRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ResponseEntity.ok(energyService.getReadingsByPeriod(userDetails.getUsername(), start, end));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReading(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        energyService.deleteReading(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Reading deleted successfully"));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(energyService.getAlerts(userDetails.getUsername()));
    }

    @PutMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<Map<String, String>> acknowledgeAlert(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        energyService.acknowledgeAlert(userDetails.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Alert acknowledged"));
    }
}
