package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.AttributeRequest;
import com.fnl33.featuretoggle.dto.AttributeResponse;
import com.fnl33.featuretoggle.domain.Attribute;
import com.fnl33.featuretoggle.service.AttributeService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Attribute management
 */
@RestController
@RequestMapping("/api/attributes")
public class AttributeController {

    private static final Logger logger = LoggerFactory.getLogger(AttributeController.class);
    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @PostMapping
    public ResponseEntity<AttributeResponse> createAttribute(@Valid @RequestBody AttributeRequest request) {
        logger.info("Creating attribute: {}", request.name());
        
        Attribute attribute = Attribute.builder()
                .name(request.name())
                .description(request.description())
                .dataType(request.dataType())
                .build();
        
        Attribute created = attributeService.create(attribute);
        AttributeResponse response = AttributeResponse.from(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AttributeResponse>> getAllAttributes() {
        logger.debug("Fetching all attributes");
        
        List<AttributeResponse> attributes = attributeService.findAll()
                .stream()
                .map(AttributeResponse::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(attributes);
    }

    @GetMapping("/{name}")
    public ResponseEntity<AttributeResponse> getAttributeByName(@PathVariable String name) {
        logger.debug("Fetching attribute by name: {}", name);
        
        Attribute attribute = attributeService.findByName(name);
        AttributeResponse response = AttributeResponse.from(attribute);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{name}")
    public ResponseEntity<AttributeResponse> updateAttribute(
            @PathVariable String name,
            @Valid @RequestBody AttributeRequest request) {
        
        logger.info("Updating attribute name: {}", name);
        
        Attribute attribute = attributeService.update(name, request.description(), request.dataType());
        AttributeResponse response = AttributeResponse.from(attribute);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable String name) {
        logger.info("Deleting attribute name: {}", name);
        
        attributeService.delete(name);
        
        return ResponseEntity.noContent().build();
    }
}
