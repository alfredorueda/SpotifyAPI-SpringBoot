package com.spotify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO record for creating a new playlist with Jakarta Bean Validation annotations.
 * 
 * Provides type-safe alternative to Map<String, Object> with compile-time validation
 * and clear API contracts. Jakarta Bean Validation annotations ensure data integrity
 * at the API boundary before reaching the service layer.
 */
public record CreatePlaylistRequest(
    
    @NotBlank(message = "Name must not be blank")
    @Size(max = 150, message = "Name must be at most 150 characters")
    String name,
    
    @NotNull(message = "isPublic field must be specified")
    Boolean isPublic
    
) {
    /**
     * Compact constructor for validation.
     * Jakarta Bean Validation annotations handle the primary validation automatically.
     */
    public CreatePlaylistRequest {
        // Validation handled by Jakarta Bean Validation annotations
    }
}