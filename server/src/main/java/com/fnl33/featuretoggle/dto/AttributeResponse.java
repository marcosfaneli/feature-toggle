package com.fnl33.featuretoggle.dto;

import com.fnl33.featuretoggle.domain.Attribute;
import com.fnl33.featuretoggle.domain.DataType;

import java.time.Instant;

public record AttributeResponse(
    String name,
    String description,
    DataType dataType,
    Instant createdAt,
    Instant updatedAt
) {
    public static AttributeResponse from(Attribute attribute) {
        return new AttributeResponse(
            attribute.getName(),
            attribute.getDescription(),
            attribute.getDataType(),
            attribute.getCreatedAt(),
            attribute.getUpdatedAt()
        );
    }
}
