package com.fnl33.featuretoggle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * RFC 7807 Problem Details for HTTP APIs
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetail(
    String type,
    String title,
    Integer status,
    String detail,
    String instance,
    Instant timestamp,
    Map<String, Object> additionalProperties
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type;
        private String title;
        private Integer status;
        private String detail;
        private String instance;
        private Instant timestamp = Instant.now();
        private Map<String, Object> additionalProperties;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder additionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        public ProblemDetail build() {
            return new ProblemDetail(type, title, status, detail, instance, timestamp, additionalProperties);
        }
    }
}
