package com.energy.repository;

import com.energy.model.Alert;
import com.energy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
    List<Alert> findByUserAndAcknowledgedFalseOrderByCreatedAtDesc(User user);
    long countByUserAndAcknowledgedFalse(User user);
}
