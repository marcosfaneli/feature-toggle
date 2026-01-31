package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.PagedResponse;
import com.fnl33.featuretoggle.dto.ToggleRequest;
import com.fnl33.featuretoggle.dto.ToggleDetailResponse;
import com.fnl33.featuretoggle.dto.ToggleListResponse;
import com.fnl33.featuretoggle.service.ToggleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<ToggleDetailResponse> createToggle(@Valid @RequestBody ToggleRequest request) {
        logger.info("Creating toggle: {}", request.name());
        
        final Toggle toggle = toggleService.create(
                request.name(),
                request.description(),
                request.enabled(),
                request.attributeName(),
                request.allowListValues()
        );
        
        final ToggleDetailResponse response = ToggleDetailResponse.from(toggle);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ToggleListResponse>> getAllToggles(Pageable pageable) {
        logger.debug("Fetching all toggles");
        
        final Page<Toggle> pagedToggle = toggleService.findAll(pageable);
        final PagedResponse<ToggleListResponse> toggles = PagedResponse.from(
            pagedToggle.map(ToggleListResponse::from)
        );
        
        return ResponseEntity.ok(toggles);
    }

    @GetMapping("/{name}")
    public ResponseEntity<ToggleDetailResponse> getToggleByName(@PathVariable String name) {
        logger.debug("Fetching toggle by name: {}", name);
        
        final Toggle toggle = toggleService.findByName(name);
        final ToggleDetailResponse response = ToggleDetailResponse.from(toggle);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{name}")
    public ResponseEntity<ToggleDetailResponse> updateToggle(
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
        
        final ToggleDetailResponse response = ToggleDetailResponse.from(toggle);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteToggle(@PathVariable String name) {
        logger.info("Deleting toggle name: {}", name);
        
        toggleService.delete(name);
        
        return ResponseEntity.noContent().build();
    }

}
