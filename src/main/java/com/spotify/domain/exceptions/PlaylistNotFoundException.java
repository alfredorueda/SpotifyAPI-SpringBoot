package com.spotify.domain.exceptions;

/**
 * Exception thrown when a playlist is not found in the system.
 * This follows DDD principles by representing a specific domain concept.
 */
public class PlaylistNotFoundException extends DomainException {
    
    public PlaylistNotFoundException(String playlistId) {
        super("Playlist not found with ID: " + playlistId);
    }
}