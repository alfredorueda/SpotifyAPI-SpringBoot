package com.spotify.domain.exceptions;

/**
 * Exception thrown when an invalid track position is specified in a playlist.
 * This follows DDD principles by representing a specific domain invariant violation.
 */
public class InvalidTrackPositionException extends DomainException {
    
    public InvalidTrackPositionException(int position, int maxPosition) {
        super("Invalid track position: " + position + ". Position must be between 0 and " + maxPosition);
    }
    
    public InvalidTrackPositionException(String message) {
        super(message);
    }
}