package com.fnl33.featuretoggle.repository;

import com.fnl33.featuretoggle.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
