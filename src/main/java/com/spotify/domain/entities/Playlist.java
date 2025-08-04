package com.spotify.domain.entities;

import com.spotify.domain.exceptions.InvalidTrackPositionException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Playlist entity representing a collection of tracks in the Spotify-like system.
 * 
 * This is a RICH DOMAIN ENTITY that follows Domain-Driven Design (DDD) principles:
 * 
 * 1. NOT AN ANEMIC MODEL: This entity encapsulates business logic and maintains domain invariants
 *    rather than being a simple data container. It includes methods like addTrack() and 
 *    addTrackAtPosition() that enforce business rules.
 * 
 * 2. APPLICATION-GENERATED IDS: Uses UUID-based alphanumeric identifiers generated within
 *    the application, not database auto-generated IDs. This design choice is crucial for:
 *    - Future microservices architecture where entities need globally unique identifiers
 *    - Distributed systems that don't rely on database sequences
 *    - Better testability and domain model isolation
 * 
 * 3. DOMAIN INVARIANT PROTECTION: The entity validates track positions, prevents invalid
 *    operations, and maintains the integrity of the track ordering through domain methods.
 * 
 * 4. ENCAPSULATED BUSINESS LOGIC: Track management logic is contained within the entity
 *    itself, following the DDD principle of keeping business rules close to the data.
 */
@Entity
@Table(name = "playlists")
public class Playlist {

    /**
     * Application-generated alphanumeric identifier using UUID.
     * This approach supports future microservices architecture by providing
     * globally unique identifiers that don't depend on database sequences.
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Ordered list of tracks in this playlist.
     * Using @OrderColumn to maintain the position/order of tracks in the playlist.
     * This mapping supports the business requirement of managing track positions.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    @OrderColumn(name = "position")
    private List<Track> tracks = new ArrayList<>();

    // Default constructor for JPA
    protected Playlist() {}

    /**
     * Constructor for creating a new playlist with application-generated ID.
     * Following DDD principles, the entity is responsible for its own identity generation.
     */
    public Playlist(String name, Boolean isPublic) {
        this.id = UUID.randomUUID().toString(); // Application-generated ID
        this.name = name;
        this.isPublic = isPublic;
        this.tracks = new ArrayList<>();
    }

    /**
     * Automatically populate creation timestamp when persisting.
     * This encapsulates the creation time logic within the entity itself.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * DOMAIN METHOD: Add a track to the end of the playlist.
     * 
     * This method encapsulates the business logic for adding tracks and demonstrates
     * that this is NOT an anemic model. The entity is responsible for maintaining
     * its own state and business rules.
     * 
     * @param track The track to add to the playlist
     */
    public void addTrack(Track track) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
        }
        // Business rule: prevent duplicate tracks in the same playlist
        if (!tracks.contains(track)) {
            tracks.add(track);
        }
    }

    /**
     * DOMAIN METHOD: Add a track at a specific position in the playlist.
     * 
     * This method demonstrates rich domain behavior by:
     * - Validating business rules (position constraints)
     * - Throwing domain-specific exceptions when invariants are violated
     * - Maintaining the integrity of the track ordering
     * 
     * @param track The track to add
     * @param position The position where to insert the track (0-based)
     * @throws InvalidTrackPositionException if the position is invalid
     */
    public void addTrackAtPosition(Track track, int position) {
        if (track == null) {
            throw new IllegalArgumentException("Track cannot be null");
        }
        
        // Domain invariant: position must be valid
        if (position < 0 || position > tracks.size()) {
            throw new InvalidTrackPositionException(position, tracks.size());
        }
        
        // Business rule: prevent duplicate tracks in the same playlist
        if (!tracks.contains(track)) {
            tracks.add(position, track);
        }
    }

    /**
     * DOMAIN METHOD: Add multiple tracks at a specific position in the playlist.
     * 
     * This method demonstrates rich domain behavior by:
     * - Validating business rules (position constraints for bulk operations)
     * - Throwing domain-specific exceptions when invariants are violated
     * - Maintaining the integrity of the track ordering for multiple insertions
     * - Using modern Java 21 features for clean implementation
     * 
     * @param tracks The list of tracks to add
     * @param position The position where to insert the tracks (0-based), or null to append at end
     * @throws InvalidTrackPositionException if the position is invalid
     */
    public void addTracks(List<Track> tracks, Integer position) {
        if (tracks == null || tracks.isEmpty()) {
            throw new IllegalArgumentException("Tracks list cannot be null or empty");
        }
        
        // If position is null, append at the end
        if (position == null) {
            tracks.stream()
                   .filter(track -> track != null && !this.tracks.contains(track))
                   .forEach(this.tracks::add);
            return;
        }
        
        // Domain invariant: position must be valid for insertion
        if (position < 0 || position > this.tracks.size()) {
            throw new InvalidTrackPositionException(position, this.tracks.size());
        }
        
        // Insert tracks at the specified position, maintaining order
        // Filter out nulls and duplicates, then insert in sequence
        List<Track> validTracks = tracks.stream()
                                       .filter(track -> track != null && !this.tracks.contains(track))
                                       .toList();
        
        // Insert tracks one by one at the position, incrementing position for each
        int currentPosition = position;
        for (Track track : validTracks) {
            this.tracks.add(currentPosition, track);
            currentPosition++;
        }
    }

    /**
     * DOMAIN METHOD: Remove a track from the playlist.
     * Encapsulates the business logic for track removal.
     */
    public boolean removeTrack(Track track) {
        return tracks.remove(track);
    }

    /**
     * DOMAIN METHOD: Get total duration of all tracks in the playlist.
     * This demonstrates business logic encapsulation within the domain entity.
     */
    public int getTotalDuration() {
        return tracks.stream()
                .mapToInt(Track::getDuration)
                .sum();
    }

    /**
     * DOMAIN METHOD: Get the number of tracks in the playlist.
     */
    public int getTrackCount() {
        return tracks.size();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns an unmodifiable view of the tracks to protect domain invariants.
     * External code cannot directly manipulate the tracks list, forcing them
     * to use the domain methods that enforce business rules.
     */
    public List<Track> getTracks() {
        return new ArrayList<>(tracks); // Return copy to protect encapsulation
    }

    // Setters for updates
    public void setName(String name) {
        this.name = name;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist playlist)) return false;
        return id != null && id.equals(playlist.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPublic=" + isPublic +
                ", trackCount=" + tracks.size() +
                ", createdAt=" + createdAt +
                '}';
    }
}