package com.energy.repository;

import com.energy.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {

    List<EnergyReading> findByUserOrderByTimestampDesc(User user);

    List<EnergyReading> findByUserAndTimestampBetweenOrderByTimestampDesc(
        User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(e.consumptionKwh) FROM EnergyReading e " +
           "WHERE e.user = :user AND e.timestamp BETWEEN :start AND :end")
    Optional<Double> sumConsumption(@Param("user") User user,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("SELECT AVG(e.consumptionKwh) FROM EnergyReading e WHERE e.user = :user")
    Optional<Double> avgConsumption(@Param("user") User user);

    @Query("SELECT MAX(e.consumptionKwh) FROM EnergyReading e WHERE e.user = :user")
    Optional<Double> maxConsumption(@Param("user") User user);

    long countByUser(User user);
}
