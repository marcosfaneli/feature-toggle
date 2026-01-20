package com.fnl33.featuretoggle.dto;

public record EvaluationResponse(
    String toggleName,
    boolean enabled,
    String value,
    String reason
) {
}
