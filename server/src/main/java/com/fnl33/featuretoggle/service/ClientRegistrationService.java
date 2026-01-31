package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.ClientRegistration;
import com.fnl33.featuretoggle.repository.ClientRegistrationRepository;
import com.fnl33.featuretoggle.repository.ToggleRepository;
import com.fnl33.featuretoggle.service.exception.ClientRegistrationNotFoundException;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientRegistrationService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final ToggleRepository toggleRepository;
    private final AuditService auditService;
    private final MetricsService metricsService;

    @Transactional(readOnly = true)
    public Page<ClientRegistration> findAll(Pageable pageable) {
        return clientRegistrationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ClientRegistration findById(UUID id) {
        return clientRegistrationRepository.findById(id)
                .orElseThrow(() -> new ClientRegistrationNotFoundException(id));
    }

    public ClientRegistration register(String callbackUrl, Set<String> toggles) {
        validateCallbackUrl(callbackUrl);
        final Set<String> togglesToPersist = toggles == null ? Set.of() : new HashSet<>(toggles);
        for (String toggleName : togglesToPersist) {
            if (!toggleRepository.existsByName(toggleName)) {
                throw new ToggleNotFoundException(toggleName);
            }
        }
        final ClientRegistration registration = ClientRegistration.builder()
                .callbackUrl(callbackUrl)
                .toggles(togglesToPersist)
                .build();
        final ClientRegistration saved = clientRegistrationRepository.save(registration);
        metricsService.incrementClientRegistered();
        auditService.logAction("REGISTER", "Client", Map.of("clientId", saved.getId(), "toggleCount", toggles.size()));
        return saved;
    }

    public void unregister(UUID id) {
        final ClientRegistration existing = findById(id);
        clientRegistrationRepository.delete(existing);
        metricsService.incrementClientUnregistered();
        auditService.logAction("UNREGISTER", "Client", Map.of("clientId", id));
    }

    @Transactional(readOnly = true)
    public List<ClientRegistration> findByToggleName(String toggleName) {
        return clientRegistrationRepository.findByTogglesContains(toggleName);
    }

    public ClientRegistration updateClientToggles(UUID id, Set<String> toggleNames) {
        final ClientRegistration client = findById(id);
        
        // Validate all toggles exist
        for (String toggleName : toggleNames) {
            if (!toggleRepository.existsByName(toggleName)) {
                throw new ToggleNotFoundException(toggleName);
            }
        }
        
        client.setToggles(new HashSet<>(toggleNames));
        final ClientRegistration saved = clientRegistrationRepository.save(client);
        auditService.logAction("UPDATE_TOGGLES", "Client", Map.of("clientId", id, "toggleCount", toggleNames.size()));
        return saved;
    }

    private void validateCallbackUrl(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            throw new ValidationException("Callback URL is required");
        }
    }
}
