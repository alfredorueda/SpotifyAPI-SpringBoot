package com.spotify.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Track entity representing a music track in the Spotify-like system.
 * 
 * This entity follows Domain-Driven Design (DDD) principles:
 * - Uses application-generated alphanumeric ID (UUID-based) instead of database auto-generated IDs
 * - This design choice prepares the system for future microservices architecture where
 *   entities need globally unique identifiers that don't depend on database sequences
 * - Encapsulates creation timestamp logic using @PrePersist
 * 
 * The entity is designed to be part of a rich domain model where business logic
 * is encapsulated within the domain layer rather than in anemic data structures.
 */
@Entity
@Table(name = "tracks")
public class Track {

    /**
     * Application-generated alphanumeric identifier.
     * Using UUID.randomUUID().toString() provides globally unique IDs
     * that are essential for distributed systems and microservices architecture.
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "artist", nullable = false, length = 255)
    private String artist;

    /**
     * Duration in seconds
     */
    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Default constructor for JPA
    protected Track() {}

    /**
     * Constructor for creating a new track with application-generated ID.
     * Following DDD principles, the entity is responsible for its own identity generation.
     */
    public Track(String title, String artist, Integer duration) {
        this.id = UUID.randomUUID().toString(); // Application-generated ID
        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    /**
     * Automatically populate creation timestamp when persisting.
     * This encapsulates the creation time logic within the entity itself.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public Integer getDuration() {
        return duration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters for updates
    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Track track)) return false;
        return id != null && id.equals(track.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Track{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", createdAt=" + createdAt +
                '}';
    }
}