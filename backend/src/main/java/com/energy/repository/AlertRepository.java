package com.energy.repository;

import com.energy.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
    List<Alert> findByUserAndAcknowledgedFalseOrderByCreatedAtDesc(User user);
    long countByUserAndAcknowledgedFalse(User user);
}
