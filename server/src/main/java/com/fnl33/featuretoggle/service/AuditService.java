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
     * Registra uma ação de auditoria com payload em formato resumido.
     *
     * @param action  Tipo de ação (CREATE, UPDATE, DELETE, etc.)
     * @param resource Tipo de recurso afetado (Toggle, Attribute, AllowListEntry, etc.)
     * @param payload Objeto com dados relevantes da ação
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
            log.error("Erro ao registrar auditoria - action: {}, resource: {}", action, resource, e);
        }
    }

    /**
     * Registra uma ação sem payload adicional
     */
    public void logAction(String action, String resource) {
        logAction(action, resource, null);
    }
}
