package com.fnl33.featuretoggle.dto;

import com.fnl33.featuretoggle.domain.Toggle;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record ToggleResponse(
    String name,
    String description,
    AttributeResponse attribute,
    boolean enabled,
    Set<String> allowListValues,
    Instant createdAt,
    Instant updatedAt
) {
    public static ToggleResponse from(Toggle toggle) {
        return new ToggleResponse(
            toggle.getName(),
            toggle.getDescription(),
            AttributeResponse.from(toggle.getAttribute()),
            toggle.isEnabled(),
            toggle.getAllowList().stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.toSet()),
            toggle.getCreatedAt(),
            toggle.getUpdatedAt()
        );
    }
}
