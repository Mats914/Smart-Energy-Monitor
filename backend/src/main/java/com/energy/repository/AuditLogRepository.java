package com.energy.repository;

import com.energy.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
