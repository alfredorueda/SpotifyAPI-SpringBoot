package com.spotify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO record for updating an existing playlist with Jakarta Bean Validation annotations.
 * 
 * Replaces Map<String, Object> with type-safe, immutable record providing
 * better maintainability, compile-time validation, and automatic runtime validation.
 */
public record UpdatePlaylistRequest(
    
    @NotBlank(message = "Name must not be blank")
    @Size(max = 150, message = "Name must be at most 150 characters")
    String name,
    
    @NotNull(message = "isPublic field must be specified")
    Boolean isPublic
    
) {
    /**
     * Compact constructor for validation.
     * Jakarta Bean Validation annotations provide automatic validation.
     */
    public UpdatePlaylistRequest {
        // Validation handled by Jakarta Bean Validation annotations
    }
}