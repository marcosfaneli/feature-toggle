package com.fnl33.featuretoggle.service.exception;

import java.util.UUID;

public class ClientRegistrationNotFoundException extends ResourceNotFoundException {
    public ClientRegistrationNotFoundException(UUID id) {
        super("Client registration %s not found".formatted(id));
    }
}
