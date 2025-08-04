package com.spotify.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO record for adding multiple tracks to a playlist with Jakarta Bean Validation annotations.
 * 
 * This record follows Java 21 best practices and provides type-safe request handling
 * for bulk track addition operations with automatic validation.
 */
public record AddMultipleTracksRequest(
    
    @NotNull(message = "Track IDs list must not be null")
    @NotEmpty(message = "Track IDs list must not be empty")
    List<@NotNull(message = "Track ID must not be null") String> trackIds,
    
    @Min(value = 0, message = "Position must be non-negative (0 or greater)")
    Integer position
    
) {
    /**
     * Compact constructor for validation.
     * Jakarta Bean Validation annotations handle basic validation,
     * while the domain layer (Playlist entity) enforces position upper bounds.
     * Position can be null to indicate appending at the end.
     */
    public AddMultipleTracksRequest {
        // Basic validation handled by Jakarta Bean Validation annotations
        // Position upper bound validation delegated to domain layer (Playlist.addTracks)
    }
}