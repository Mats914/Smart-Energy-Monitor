package com.energy.repository;

import com.energy.model.EnergyReading;
import com.energy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {

    List<EnergyReading> findByUserOrderByTimestampDesc(User user);

    List<EnergyReading> findByUserAndTimestampBetweenOrderByTimestampDesc(
            User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(e.consumptionKwh) FROM EnergyReading e WHERE e.user = :user " +
           "AND e.timestamp >= :start AND e.timestamp <= :end")
    Optional<Double> sumConsumptionByUserAndPeriod(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT AVG(e.consumptionKwh) FROM EnergyReading e WHERE e.user = :user")
    Optional<Double> avgConsumptionByUser(@Param("user") User user);

    @Query("SELECT MAX(e.consumptionKwh) FROM EnergyReading e WHERE e.user = :user")
    Optional<Double> maxConsumptionByUser(@Param("user") User user);

    long countByUser(User user);
}
