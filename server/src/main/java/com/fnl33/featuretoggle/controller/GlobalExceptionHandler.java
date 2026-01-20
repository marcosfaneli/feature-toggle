package com.fnl33.featuretoggle.controller;

import com.fnl33.featuretoggle.dto.ProblemDetail;
import com.fnl33.featuretoggle.service.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that returns RFC 7807 Problem Details
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.builder()
                .type("about:blank")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        
        logger.warn("Validation error: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.builder()
                .type("about:blank")
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        logger.warn("Validation error in request body");
        
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ProblemDetail problem = ProblemDetail.builder()
                .type("about:blank")
                .title("Validation Error")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Invalid request parameters")
                .instance(request.getRequestURI())
                .additionalProperties(Map.of("errors", errors))
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(AttributeInUseException.class)
    public ResponseEntity<ProblemDetail> handleAttributeInUse(
            AttributeInUseException ex,
            HttpServletRequest request) {
        
        logger.warn("Attribute in use: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.builder()
                .type("about:blank")
                .title("Attribute In Use")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {
        
        logger.error("Unexpected error", ex);
        
        ProblemDetail problem = ProblemDetail.builder()
                .type("about:blank")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("An unexpected error occurred")
                .instance(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
