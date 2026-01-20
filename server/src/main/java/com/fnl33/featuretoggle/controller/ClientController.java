package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.ClientRegistrationRequest;
import com.fnl33.featuretoggle.dto.ClientRegistrationResponse;
import com.fnl33.featuretoggle.domain.ClientRegistration;
import com.fnl33.featuretoggle.service.ClientRegistrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Client Registration management
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final ClientRegistrationService clientRegistrationService;

    public ClientController(ClientRegistrationService clientRegistrationService) {
        this.clientRegistrationService = clientRegistrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<ClientRegistrationResponse> registerClient(
            @Valid @RequestBody ClientRegistrationRequest request) {
        
        logger.info("Registering client for callback: {}", request.callbackUrl());
        
        ClientRegistration client = clientRegistrationService.register(
                request.callbackUrl(),
                request.toggleNames()
        );
        
        ClientRegistrationResponse response = ClientRegistrationResponse.from(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientRegistrationResponse> getClient(@PathVariable UUID id) {
        logger.debug("Fetching client by id: {}", id);
        
        ClientRegistration client = clientRegistrationService.findById(id);
        ClientRegistrationResponse response = ClientRegistrationResponse.from(client);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientRegistrationResponse>> getAllClients() {
        logger.debug("Fetching all clients");
        
        List<ClientRegistrationResponse> clients = clientRegistrationService.findAll()
                .stream()
                .map(ClientRegistrationResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(clients);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unregisterClient(@PathVariable UUID id) {
        logger.info("Unregistering client id: {}", id);
        
        clientRegistrationService.unregister(id);
        
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggles")
    public ResponseEntity<ClientRegistrationResponse> updateClientToggles(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRegistrationRequest request) {
        
        logger.info("Updating toggles for client id: {}", id);
        
        ClientRegistration client = clientRegistrationService.updateClientToggles(
                id,
                request.toggleNames()
        );
        
        ClientRegistrationResponse response = ClientRegistrationResponse.from(client);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/toggle/{toggleName}")
    public ResponseEntity<List<ClientRegistrationResponse>> getClientsByToggleName(
            @PathVariable String toggleName) {
        
        logger.debug("Fetching clients for toggle: {}", toggleName);
        
        List<ClientRegistrationResponse> clients = clientRegistrationService
                .findByToggleName(toggleName)
                .stream()
                .map(ClientRegistrationResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(clients);
    }
}
