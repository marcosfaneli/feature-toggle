package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.AuditLog;
import com.fnl33.featuretoggle.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Logs an audit action with payload in summary format.
     *
     * @param action  Action type (CREATE, UPDATE, DELETE, etc.)
     * @param resource Resource type affected (Toggle, Attribute, AllowListEntry, etc.)
     * @param payload Object with relevant action data
     */
    public void logAction(String action, String resource, Object payload) {
        try {
            String payloadJson = payload != null ? objectMapper.writeValueAsString(payload) : null;
            
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .resource(resource)
                    .payload(payloadJson)
                    .build();

            auditLogRepository.save(auditLog);
            
            log.info("Audit: action={} resource={} timestamp={}", 
                    action, resource, auditLog.getCreatedAt());
        } catch (Exception e) {
            log.error("Error logging audit action - action: {}, resource: {}", action, resource, e);
        }
    }

    /**
     * Logs an action without additional payload
     */
    public void logAction(String action, String resource) {
        logAction(action, resource, null);
    }
}
