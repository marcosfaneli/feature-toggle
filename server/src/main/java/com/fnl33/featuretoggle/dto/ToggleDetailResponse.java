package com.fnl33.featuretoggle.dto;

import com.fnl33.featuretoggle.domain.Toggle;

import java.time.Instant;

public record ToggleDetailResponse(
    String name,
    String description,
    AttributeResponse attribute,
    boolean enabled,
    Instant createdAt,
    Instant updatedAt
) {
    public static ToggleDetailResponse from(Toggle toggle) {
        return new ToggleDetailResponse(
            toggle.getName(),
            toggle.getDescription(),
            AttributeResponse.from(toggle.getAttribute()),
            toggle.isEnabled(),
            toggle.getCreatedAt(),
            toggle.getUpdatedAt()
        );
    }
}
