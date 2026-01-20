package com.fnl33.featuretoggle.dto;

import com.fnl33.featuretoggle.domain.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttributeRequest(
    @NotBlank(message = "Attribute name is required")
    String name,

    String description,

    @NotNull(message = "Data type is required")
    DataType dataType
) {
}
