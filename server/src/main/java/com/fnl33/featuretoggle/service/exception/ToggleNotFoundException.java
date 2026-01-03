package com.fnl33.featuretoggle.service.exception;

public class ToggleNotFoundException extends ResourceNotFoundException {
    public ToggleNotFoundException(String name) {
        super("Toggle %s not found".formatted(name));
    }
}
