package com.spotify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

/**
 * DTO record for creating a new track with Jakarta Bean Validation annotations.
 * 
 * ENHANCED WITH VALIDATION: This record now includes Jakarta Bean Validation (JSR-380)
 * annotations that provide automatic request validation at the API boundary.
 * 
 * Benefits of validation annotations:
 * - Automatic validation before reaching service layer
 * - Consistent error messages across the API
 * - Reduced boilerplate validation code
 * - Clear API contract documentation through annotations
 * - Integration with Spring Boot's validation framework
 * 
 * When used with @Valid in controllers, Spring Boot automatically validates
 * incoming requests and returns structured error responses for invalid data.
 */
public record CreateTrackRequest(
    
    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must be at most 100 characters")
    String title,
    
    @NotBlank(message = "Artist must not be blank") 
    @Size(max = 100, message = "Artist must be at most 100 characters")
    String artist,
    
    @NotNull(message = "Duration must not be null")
    @Positive(message = "Duration must be a positive number in seconds")
    Integer duration
    
) {
    /**
     * Compact constructor for additional business logic validation.
     * Note: Jakarta Bean Validation annotations are processed first,
     * then this constructor runs for any additional custom validation.
     */
    public CreateTrackRequest {
        // Jakarta Bean Validation annotations handle the primary validation
        // Custom validation logic can be added here if needed
    }
}