package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.Attribute;
import com.fnl33.featuretoggle.domain.DataType;
import com.fnl33.featuretoggle.repository.AttributeRepository;
import com.fnl33.featuretoggle.repository.ToggleRepository;
import com.fnl33.featuretoggle.service.exception.AttributeInUseException;
import com.fnl33.featuretoggle.service.exception.AttributeNotFoundException;
import com.fnl33.featuretoggle.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final ToggleRepository toggleRepository;
    private final NotificationOrchestrator notificationOrchestrator;
    private final AuditService auditService;
    private final MetricsService metricsService;

    @Transactional(readOnly = true)
    public List<Attribute> findAll() {
        return attributeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Attribute findByName(String name) {
        return attributeRepository.findById(name)
                .orElseThrow(() -> new AttributeNotFoundException(name));
    }

    public Attribute create(Attribute attribute) {
        validateAttribute(attribute);
        if (attributeRepository.existsByName(attribute.getName())) {
            throw new ValidationException("Attribute with name %s already exists".formatted(attribute.getName()));
        }
        final Attribute saved = attributeRepository.save(attribute);
        metricsService.incrementAttributeCreated();
        auditService.logAction("CREATE", "Attribute", Map.of("name", attribute.getName(), "dataType", attribute.getDataType()));
        return saved;
    }

    public Attribute update(String name, String description, DataType dataType) {
        final Attribute existing = findByName(name);
        if (dataType == null) {
            throw new ValidationException("Attribute data type is required");
        }
        existing.setDescription(description);
        existing.setDataType(dataType);
        final Attribute saved = attributeRepository.save(existing);
        metricsService.incrementAttributeUpdated();
        auditService.logAction("UPDATE", "Attribute", Map.of("name", name, "dataType", dataType));
        notificationOrchestrator.notifyAttributeChange(saved,
                toggleRepository.findByAttribute_Name(name).stream().map(toggle -> toggle.getName()).toList());
        return saved;
    }

    public void delete(String name) {
        final Attribute existing = findByName(name);
        if (toggleRepository.existsByAttribute_Name(name)) {
            throw new AttributeInUseException(name);
        }
        attributeRepository.delete(existing);
        metricsService.incrementAttributeDeleted();
        auditService.logAction("DELETE", "Attribute", Map.of("name", name));
        notificationOrchestrator.notifyAttributeChange(existing, List.of());
    }

    private void validateAttribute(Attribute attribute) {
        if (attribute == null) {
            throw new ValidationException("Attribute payload is required");
        }
        if (attribute.getName() == null || attribute.getName().isBlank()) {
            throw new ValidationException("Attribute name is required");
        }
        if (attribute.getDataType() == null) {
            throw new ValidationException("Attribute data type is required");
        }
    }
}
