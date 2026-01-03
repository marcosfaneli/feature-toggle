package com.fnl33.featuretoggle.service.exception;

public class AttributeInUseException extends ValidationException {
    public AttributeInUseException(String name) {
        super("Attribute %s is referenced by toggles".formatted(name));
    }
}
