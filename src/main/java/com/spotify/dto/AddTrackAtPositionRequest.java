package com.spotify.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO record for adding a track at a specific position in a playlist with Jakarta Bean Validation annotations.
 * 
 * Type-safe alternative to Map<String, Object> that provides compile-time validation
 * for both track ID and position parameters, improving API reliability with automatic validation.
 */
public record AddTrackAtPositionRequest(
    
    @NotBlank(message = "Track ID must not be blank")
    String trackId,
    
    @NotNull(message = "Position must be specified")
    @Min(value = 0, message = "Position must be non-negative (0 or greater)")
    Integer position
    
) {
    /**
     * Compact constructor for validation.
     * Jakarta Bean Validation annotations handle basic validation,
     * while the domain layer (Playlist entity) enforces position upper bounds.
     */
    public AddTrackAtPositionRequest {
        // Basic validation handled by Jakarta Bean Validation annotations
        // Position upper bound validation delegated to domain layer (Playlist.addTrackAtPosition)
    }
}