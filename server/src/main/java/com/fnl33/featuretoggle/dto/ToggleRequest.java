package com.fnl33.featuretoggle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ToggleRequest(
    @NotBlank(message = "Toggle name is required")
    String name,

    String description,

    @NotNull(message = "Enabled flag is required")
    Boolean enabled,

    @NotBlank(message = "Attribute name is required")
    String attributeName,

    List<String> allowListValues
) {
}
