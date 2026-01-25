package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.AllowListRequest;
import com.fnl33.featuretoggle.dto.ToggleRequest;
import com.fnl33.featuretoggle.dto.ToggleResponse;
import com.fnl33.featuretoggle.domain.Toggle;
import com.fnl33.featuretoggle.service.ToggleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for Toggle management
 */
@RestController
@RequestMapping("/api/toggles")
public class ToggleController {

    private static final Logger logger = LoggerFactory.getLogger(ToggleController.class);
    private final ToggleService toggleService;

    public ToggleController(ToggleService toggleService) {
        this.toggleService = toggleService;
    }

    @PostMapping
    public ResponseEntity<ToggleResponse> createToggle(@Valid @RequestBody ToggleRequest request) {
        logger.info("Creating toggle: {}", request.name());
        
        final Toggle toggle = toggleService.create(
                request.name(),
                request.description(),
                request.enabled(),
                request.attributeName(),
                request.allowListValues()
        );
        
        final ToggleResponse response = ToggleResponse.from(toggle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ToggleResponse>> getAllToggles() {
        logger.debug("Fetching all toggles");
        
        final List<ToggleResponse> toggles = toggleService.findAll()
                .stream()
                .map(ToggleResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(toggles);
    }

    @GetMapping("/{name}")
    public ResponseEntity<ToggleResponse> getToggleByName(@PathVariable String name) {
        logger.debug("Fetching toggle by name: {}", name);
        
        final Toggle toggle = toggleService.findByName(name);
        final ToggleResponse response = ToggleResponse.from(toggle);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{name}")
    public ResponseEntity<ToggleResponse> updateToggle(
            @PathVariable String name,
            @Valid @RequestBody ToggleRequest request) {
        
        logger.info("Updating toggle name: {}", name);
        
        final Toggle toggle = toggleService.update(
                name,
                request.description(),
                request.enabled(),
                request.attributeName(),
                request.allowListValues()
        );
        
        final ToggleResponse response = ToggleResponse.from(toggle);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteToggle(@PathVariable String name) {
        logger.info("Deleting toggle name: {}", name);
        
        toggleService.delete(name);
        
        return ResponseEntity.noContent().build();
    }

    // Allow List Management

    @PostMapping("/{name}/allow-list")
    public ResponseEntity<ToggleResponse> addToAllowList(
            @PathVariable String name,
            @RequestParam String value) {
        
        logger.info("Adding value to allow list for toggle name: {}", name);
        
        toggleService.addAllowListEntry(name, value);
        final Toggle toggle = toggleService.findByName(name);
        final ToggleResponse response = ToggleResponse.from(toggle);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}/allow-list")
    public ResponseEntity<ToggleResponse> removeFromAllowList(
            @PathVariable String name,
            @RequestParam String value) {
        
        logger.info("Removing value from allow list for toggle name: {}", name);
        
        toggleService.removeAllowListEntry(name, value);
        final Toggle toggle = toggleService.findByName(name);
        final ToggleResponse response = ToggleResponse.from(toggle);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}/allow-list")
    public ResponseEntity<Set<String>> getAllowList(@PathVariable String name) {
        logger.debug("Fetching allow list for toggle name: {}", name);
        
        final Toggle toggle = toggleService.findByName(name);
        final Set<String> allowList = toggle.getAllowList().stream()
                .map(entry -> entry.getValue())
                .collect(Collectors.toSet());
        
        return ResponseEntity.ok(allowList);
    }
}
