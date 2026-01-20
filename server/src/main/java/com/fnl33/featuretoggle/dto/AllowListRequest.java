package com.fnl33.featuretoggle.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record AllowListRequest(
    @NotEmpty(message = "At least one value is required")
    Set<String> values
) {
}
