package com.spotify.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO record for adding a track to a playlist with Jakarta Bean Validation annotations.
 * 
 * Eliminates unsafe casting from Map<String, Object> and provides
 * clear, type-safe API contract with automatic validation for track addition operations.
 */
public record AddTrackToPlaylistRequest(
    
    @NotBlank(message = "Track ID must not be blank")
    String trackId
    
) {
    /**
     * Compact constructor for validation.
     * Jakarta Bean Validation annotations handle validation automatically.
     */
    public AddTrackToPlaylistRequest {
        // Validation handled by Jakarta Bean Validation annotations
    }
}