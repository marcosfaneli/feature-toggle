package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.EvaluationResponse;
import com.fnl33.featuretoggle.service.EvaluationResult;
import com.fnl33.featuretoggle.service.EvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Toggle evaluation
 */
@RestController
@RequestMapping("/api/toggles")
public class EvaluationController {

    private static final Logger logger = LoggerFactory.getLogger(EvaluationController.class);
    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/{name}/evaluate")
    public ResponseEntity<EvaluationResponse> evaluateToggle(
            @PathVariable String name,
            @RequestParam(required = false) String value) {
        
        logger.debug("Evaluating toggle: {} with value: {}", name, value);
        
        final EvaluationResult result = evaluationService.evaluate(name, value);
        
        final EvaluationResponse response = new EvaluationResponse(
                name,
                result.enabled(),
                value,
                result.reason()
        );
        
        return ResponseEntity.ok(response);
    }
}
