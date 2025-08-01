package com.spotify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO record for updating an existing track with Jakarta Bean Validation annotations.
 * 
 * This record provides type safety and automatic validation for track updates,
 * replacing unsafe Map<String, Object> casting with compile-time type checking
 * and runtime validation.
 */
public record UpdateTrackRequest(
    
    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must be at most 100 characters")
    String title,
    
    @NotBlank(message = "Artist must not be blank")
    @Size(max = 100, message = "Artist must be at most 100 characters")
    String artist,
    
    @Positive(message = "Duration must be a positive number in seconds")
    Integer duration
    
) {
    /**
     * Compact constructor for validation.
     * Jakarta Bean Validation annotations provide the primary validation.
     */
    public UpdateTrackRequest {
        // Validation handled by annotations
    }
}