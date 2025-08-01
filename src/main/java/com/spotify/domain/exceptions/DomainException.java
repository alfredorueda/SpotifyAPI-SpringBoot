package com.spotify.domain.exceptions;

/**
 * Base domain exception for all business logic violations.
 * Following DDD principles, this exception represents domain-specific errors
 * that occur when business rules or invariants are violated.
 */
public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}