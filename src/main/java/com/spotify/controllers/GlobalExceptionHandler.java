package com.spotify.controllers;

import com.spotify.domain.exceptions.DomainException;
import com.spotify.domain.exceptions.InvalidTrackPositionException;
import com.spotify.domain.exceptions.PlaylistNotFoundException;
import com.spotify.domain.exceptions.TrackNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Spotify API.
 * 
 * ENHANCED WITH VALIDATION ERROR HANDLING: Now includes comprehensive handling
 * for Jakarta Bean Validation errors via MethodArgumentNotValidException.
 * 
 * This provides structured JSON error responses with appropriate HTTP status codes
 * for both domain exceptions and validation failures, ensuring consistent
 * error handling across the entire API.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Jakarta Bean Validation errors from @Valid annotations.
     * 
     * NEW: Handles MethodArgumentNotValidException thrown when @Valid validation fails
     * on request DTOs. Provides detailed field-level validation error information
     * in a structured JSON response.
     * 
     * This integrates seamlessly with our Jakarta Bean Validation annotations:
     * - @NotBlank, @Size, @Positive, @Min, etc.
     * - Returns 400 BAD REQUEST with detailed validation error information
     * - Maps each field error to a clear, user-friendly response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "VALIDATION_ERROR");
        errorResponse.put("message", "Request validation failed");
        
        // Extract field-level validation errors
        errorResponse.put("fieldErrors", ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> {
                Object rejectedValue = fieldError.getRejectedValue();
                return Map.of(
                    "field", fieldError.getField(),
                    "rejectedValue", rejectedValue != null ? rejectedValue.toString() : "null",
                    "message", fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                );
            })
            .toList());
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle Track not found exceptions.
     */
    @ExceptionHandler(TrackNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTrackNotFoundException(TrackNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), "TRACK_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Playlist not found exceptions.
     */
    @ExceptionHandler(PlaylistNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePlaylistNotFoundException(PlaylistNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), "PLAYLIST_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Invalid track position exceptions.
     */
    @ExceptionHandler(InvalidTrackPositionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTrackPositionException(InvalidTrackPositionException ex) {
        return createErrorResponse(ex.getMessage(), "INVALID_TRACK_POSITION", HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle general domain exceptions.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex) {
        return createErrorResponse(ex.getMessage(), "DOMAIN_ERROR", HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions (validation errors).
     * 
     * NOTE: With Jakarta Bean Validation, most validation errors will be caught
     * by handleValidationException instead. This handler remains for any
     * custom validation logic in domain entities or services.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return createErrorResponse(ex.getMessage(), "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle general exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        return createErrorResponse("An unexpected error occurred", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Create structured error response following REST API best practices.
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, String errorType, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", errorType);
        errorResponse.put("message", message);
        
        return new ResponseEntity<>(errorResponse, status);
    }
}