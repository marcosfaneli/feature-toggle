package com.fnl33.featuretoggle.service.exception;

public class AllowListEntryNotFoundException extends ResourceNotFoundException {
    public AllowListEntryNotFoundException(String toggleName, String value) {
        super("Value %s not found in allow list for toggle %s".formatted(value, toggleName));
    }
}
