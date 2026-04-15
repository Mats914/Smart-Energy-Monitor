package com.energy.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double consumptionKwh;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String location;

    @Enumerated(EnumType.STRING)
    private ReadingSource source;

    public enum ReadingSource {
        MANUAL, SIMULATED, SENSOR
    }
}
