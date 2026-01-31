package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.AllowListEntry;
import com.fnl33.featuretoggle.domain.Attribute;
import com.fnl33.featuretoggle.domain.Toggle;
import com.fnl33.featuretoggle.repository.AllowListEntryRepository;
import com.fnl33.featuretoggle.repository.AttributeRepository;
import com.fnl33.featuretoggle.repository.ToggleRepository;
import com.fnl33.featuretoggle.service.exception.AllowListEntryNotFoundException;
import com.fnl33.featuretoggle.service.exception.AttributeNotFoundException;
import com.fnl33.featuretoggle.service.exception.ToggleNotFoundException;
import com.fnl33.featuretoggle.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ToggleService {

    private final ToggleRepository toggleRepository;
    private final AttributeRepository attributeRepository;
    private final AllowListEntryRepository allowListEntryRepository;
    private final NotificationOrchestrator notificationOrchestrator;
    private final AuditService auditService;
    private final MetricsService metricsService;

    @Transactional(readOnly = true)
    public Page<Toggle> findAll(Pageable pageable) {
        return toggleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Toggle findByName(String name) {
        return toggleRepository.findByName(name)
                .orElseThrow(() -> new ToggleNotFoundException(name));
    }

    @Transactional(readOnly = true)
    public Page<String> findAllowListValues(String toggleName, Pageable pageable) {
        findByName(toggleName);
        return allowListEntryRepository.findByToggle_Name(toggleName, pageable)
                .map(AllowListEntry::getValue);
    }

    public Toggle create(String name, String description, boolean enabled, String attributeName, List<String> allowListValues) {
        validateToggleName(name);
        if (toggleRepository.existsByName(name)) {
            throw new ValidationException("Toggle with name %s already exists".formatted(name));
        }
        final Attribute attribute = resolveAttribute(attributeName);
        final Toggle toggle = Toggle.builder()
                .name(name)
                .description(description)
                .enabled(enabled)
                .attribute(attribute)
                .build();
        syncAllowList(toggle, allowListValues);
        final Toggle saved = toggleRepository.save(toggle);
        metricsService.incrementToggleCreated();
        auditService.logAction("CREATE", "Toggle", Map.of("name", name, "enabled", enabled, "attribute", attributeName));
        notificationOrchestrator.notifyToggleChange(saved, null);
        return saved;
    }

    public Toggle update(String name, String description, boolean enabled, String attributeName, List<String> allowListValues) {
        final Toggle existing = findByName(name);
        final Attribute attribute = resolveAttribute(attributeName);
        existing.setDescription(description);
        existing.setEnabled(enabled);
        existing.setAttribute(attribute);
        syncAllowList(existing, allowListValues);
        final Toggle saved = toggleRepository.save(existing);
        metricsService.incrementToggleUpdated();
        auditService.logAction("UPDATE", "Toggle", Map.of("name", name, "enabled", enabled, "attribute", attributeName));
        notificationOrchestrator.notifyToggleChange(saved, null);
        return saved;
    }

    public void delete(String name) {
        final Toggle existing = findByName(name);
        toggleRepository.delete(existing);
        metricsService.incrementToggleDeleted();
        auditService.logAction("DELETE", "Toggle", Map.of("name", name));
        notificationOrchestrator.notifyToggleChange(existing, null);
    }

    public AllowListEntry addAllowListEntry(String toggleName, String value) {
        final Toggle toggle = findByName(toggleName);
        validateAllowListValue(value);
        if (allowListEntryRepository.existsByToggle_NameAndValue(toggleName, value)) {
            throw new ValidationException("Value already present in allow list for toggle %s".formatted(toggleName));
        }
        final AllowListEntry entry = AllowListEntry.builder()
                .toggle(toggle)
                .value(value)
                .build();
        toggle.getAllowList().add(entry);
        final Toggle saved = toggleRepository.save(toggle);
        auditService.logAction("ADD_ALLOW_LIST", "Toggle", Map.of("toggleName", toggleName, "value", value));
        notificationOrchestrator.notifyToggleChange(saved, value);
        return entry;
    }

    public void removeAllowListEntry(String toggleName, String value) {
        final Toggle toggle = findByName(toggleName);
        final AllowListEntry entry = allowListEntryRepository.findByToggle_NameAndValue(toggleName, value)
                .orElseThrow(() -> new AllowListEntryNotFoundException(toggleName, value));
        toggle.getAllowList().remove(entry);
        allowListEntryRepository.delete(entry);
        auditService.logAction("REMOVE_ALLOW_LIST", "Toggle", Map.of("toggleName", toggleName, "value", value));
        notificationOrchestrator.notifyToggleChange(toggle, value);
    }

    private Attribute resolveAttribute(String attributeName) {
        if (attributeName == null || attributeName.isBlank()) {
            throw new ValidationException("Attribute name is required");
        }
        return attributeRepository.findByName(attributeName)
                .orElseThrow(() -> new AttributeNotFoundException(attributeName));
    }

    private void syncAllowList(Toggle toggle, List<String> allowListValues) {
        toggle.getAllowList().clear();
        if (allowListValues == null || allowListValues.isEmpty()) {
            return;
        }
        Set<String> uniqueValues = new HashSet<>();
        for (String value : allowListValues) {
            validateAllowListValue(value);
            if (!uniqueValues.add(value)) {
                throw new ValidationException("Duplicate allow list value: %s".formatted(value));
            }
            toggle.getAllowList().add(AllowListEntry.builder().toggle(toggle).value(value).build());
        }
    }

    private void validateAllowListValue(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Allow list value is required");
        }
    }

    private void validateToggleName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Toggle name is required");
        }
    }
}
