package com.fnl33.featuretoggle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ClientRegistrationRequest(
    @NotBlank(message = "Callback URL is required")
    String callbackUrl,

    @NotEmpty(message = "At least one toggle name is required")
    Set<String> toggleNames
) {
}
