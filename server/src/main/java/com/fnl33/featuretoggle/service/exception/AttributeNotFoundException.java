package com.fnl33.featuretoggle.service.exception;

public class AttributeNotFoundException extends ResourceNotFoundException {
    public AttributeNotFoundException(String name) {
        super("Attribute %s not found".formatted(name));
    }
}
