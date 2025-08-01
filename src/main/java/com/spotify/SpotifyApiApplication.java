package com.spotify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Spotify-like REST API.
 * This application follows Domain-Driven Design (DDD) principles with a rich domain model.
 * 
 * Key architectural decisions:
 * - Uses application-generated alphanumeric IDs (UUID-based) for entities instead of 
 *   database auto-generated numeric IDs to prepare for future microservices architecture
 * - Implements rich domain entities that encapsulate business logic and maintain invariants
 * - Follows REST API best practices with proper HTTP methods and status codes
 */
@SpringBootApplication
public class SpotifyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifyApiApplication.class, args);
    }
}