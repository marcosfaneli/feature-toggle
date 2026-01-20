package com.fnl33.featuretoggle.service;

import com.fnl33.featuretoggle.domain.ClientRegistration;
import com.fnl33.featuretoggle.repository.ClientRegistrationRepository;
import com.fnl33.featuretoggle.repository.ToggleRepository;
import com.fnl33.featuretoggle.service.exception.ClientRegistrationNotFoundException;
import com.fnl33.featuretoggle.service.exception.ToggleNotFoundException;
import com.fnl33.featuretoggle.service.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientRegistrationService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final ToggleRepository toggleRepository;

    @Transactional(readOnly = true)
    public List<ClientRegistration> findAll() {
        return clientRegistrationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ClientRegistration findById(UUID id) {
        return clientRegistrationRepository.findById(id)
                .orElseThrow(() -> new ClientRegistrationNotFoundException(id));
    }

    public ClientRegistration register(String callbackUrl, Set<String> toggles) {
        validateCallbackUrl(callbackUrl);
        Set<String> togglesToPersist = toggles == null ? Set.of() : new HashSet<>(toggles);
        for (String toggleName : togglesToPersist) {
            if (!toggleRepository.existsByName(toggleName)) {
                throw new ToggleNotFoundException(toggleName);
            }
        }
        ClientRegistration registration = ClientRegistration.builder()
                .callbackUrl(callbackUrl)
                .toggles(togglesToPersist)
                .build();
        return clientRegistrationRepository.save(registration);
    }

    public void unregister(UUID id) {
        ClientRegistration existing = findById(id);
        clientRegistrationRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<ClientRegistration> findByToggleName(String toggleName) {
        return clientRegistrationRepository.findByTogglesContains(toggleName);
    }

    public ClientRegistration updateClientToggles(UUID id, Set<String> toggleNames) {
        ClientRegistration client = findById(id);
        
        // Validate all toggles exist
        for (String toggleName : toggleNames) {
            if (!toggleRepository.existsByName(toggleName)) {
                throw new ToggleNotFoundException(toggleName);
            }
        }
        
        client.setToggles(new HashSet<>(toggleNames));
        return clientRegistrationRepository.save(client);
    }

    private void validateCallbackUrl(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            throw new ValidationException("Callback URL is required");
        }
    }
}
