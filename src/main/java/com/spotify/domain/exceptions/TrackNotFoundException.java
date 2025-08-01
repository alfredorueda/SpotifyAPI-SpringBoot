package com.spotify.domain.exceptions;

/**
 * Exception thrown when a track is not found in the system.
 * This follows DDD principles by representing a specific domain concept.
 */
public class TrackNotFoundException extends DomainException {
    
    public TrackNotFoundException(String trackId) {
        super("Track not found with ID: " + trackId);
    }
}