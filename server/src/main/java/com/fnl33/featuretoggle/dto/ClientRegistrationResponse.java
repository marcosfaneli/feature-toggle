package com.fnl33.featuretoggle.dto;

import com.fnl33.featuretoggle.domain.ClientRegistration;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ClientRegistrationResponse(
    UUID id,
    String callbackUrl,
    Set<String> toggleNames,
    Instant createdAt,
    Instant updatedAt
) {
    public static ClientRegistrationResponse from(ClientRegistration client) {
        return new ClientRegistrationResponse(
            client.getId(),
            client.getCallbackUrl(),
            client.getToggles(),
            client.getCreatedAt(),
            client.getUpdatedAt()
        );
    }
}
