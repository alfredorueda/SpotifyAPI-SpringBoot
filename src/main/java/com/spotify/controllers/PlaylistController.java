package com.spotify.controllers;

import com.spotify.domain.entities.Playlist;
import com.spotify.domain.entities.Track;
import com.spotify.dto.AddMultipleTracksRequest;
import com.spotify.dto.AddTrackAtPositionRequest;
import com.spotify.dto.AddTrackToPlaylistRequest;
import com.spotify.dto.CreatePlaylistRequest;
import com.spotify.dto.UpdatePlaylistRequest;
import com.spotify.services.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for Playlist operations.
 * 
 * ENHANCED WITH AUTOMATIC VALIDATION: Now leverages @Valid annotations with
 * Jakarta Bean Validation for comprehensive request validation at the API boundary.
 * 
 * Validation improvements:
 * - @Valid triggers automatic validation before service method execution
 * - DTO validation annotations ensure data integrity and consistency
 * - Structured error responses for validation failures
 * - Eliminates manual validation boilerplate in controller methods
 * - Clear separation of concerns: annotations define rules, @Valid enforces them
 * 
 * This transformation provides significant improvements:
 * - Type safety: No more runtime ClassCastException from unsafe casting
 * - API documentation: DTOs serve as self-documenting contracts with validation rules
 * - Validation: Built-in validation through Jakarta Bean Validation annotations
 * - Maintainability: IDE support for refactoring and autocomplete
 * - Performance: Direct field access with automatic validation guarantees
 * 
 * Follows REST API best practices with proper HTTP methods, status codes, and resource naming.
 */
@RestController
@RequestMapping("/playlists")
@Validated
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * Constructor-based dependency injection (Spring best practice).
     */
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    /**
     * GET /playlists - Retrieve all playlists
     * Returns 200 OK with list of playlists
     */
    @GetMapping
    public ResponseEntity<List<Playlist>> getAllPlaylists() {
        List<Playlist> playlists = playlistService.getAllPlaylists();
        return ResponseEntity.ok(playlists);
    }

    /**
     * GET /playlists/{id} - Retrieve a specific playlist by ID
     * Returns 200 OK if found, 404 NOT FOUND if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Playlist> getPlaylistById(@PathVariable String id) {
        Playlist playlist = playlistService.getPlaylistById(id);
        return ResponseEntity.ok(playlist);
    }

    /**
     * POST /playlists - Create a new playlist using validated DTO
     * 
     * ENHANCED WITH @Valid: Automatic validation for playlist creation
     * - @Valid triggers Jakarta Bean Validation on CreatePlaylistRequest
     * - Validates name length, blank checks, and isPublic field requirements
     * - Returns structured validation errors if constraints are violated
     * 
     * Returns 201 CREATED with the created playlist
     */
    @PostMapping
    public ResponseEntity<Playlist> createPlaylist(@Valid @RequestBody CreatePlaylistRequest request) {
        // Validation completed successfully - all constraints satisfied
        Playlist playlist = playlistService.createPlaylist(request.name(), request.isPublic());
        return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
    }

    /**
     * PUT /playlists/{id} - Update an existing playlist using validated DTO
     * 
     * ENHANCED WITH @Valid: Automatic validation for playlist updates
     * - Ensures name and isPublic field constraints are met
     * - Type-safe field access with validation guarantees
     * 
     * Returns 200 OK with the updated playlist
     */
    @PutMapping("/{id}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable String id, @Valid @RequestBody UpdatePlaylistRequest request) {
        // All validation constraints satisfied at this point
        Playlist playlist = playlistService.updatePlaylist(id, request.name(), request.isPublic());
        return ResponseEntity.ok(playlist);
    }

    /**
     * DELETE /playlists/{id} - Delete a playlist
     * Returns 204 NO CONTENT when successfully deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable String id) {
        playlistService.deletePlaylist(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /playlists/{playlistId}/tracks - Get all tracks in a playlist
     * Returns 200 OK with list of tracks in the playlist
     */
    @GetMapping("/{playlistId}/tracks")
    public ResponseEntity<List<Track>> getPlaylistTracks(@PathVariable String playlistId) {
        List<Track> tracks = playlistService.getPlaylistTracks(playlistId);
        return ResponseEntity.ok(tracks);
    }

    /**
     * POST /playlists/{playlistId}/tracks - Add a track to the end of a playlist
     * 
     * ENHANCED WITH @Valid: Automatic validation for track addition
     * - @Valid ensures trackId is not blank via @NotBlank annotation
     * - Eliminates need for manual null/empty checks in controller
     * - Clean, validated parameter passing to service layer
     * 
     * Returns 200 OK with the updated playlist
     */
    @PostMapping("/{playlistId}/tracks")
    public ResponseEntity<Playlist> addTrackToPlaylist(@PathVariable String playlistId, 
                                                       @Valid @RequestBody AddTrackToPlaylistRequest request) {
        // trackId validated as non-blank by Jakarta Bean Validation
        Playlist playlist = playlistService.addTrackToPlaylist(playlistId, request.trackId());
        return ResponseEntity.ok(playlist);
    }

    /**
     * POST /playlists/{playlistId}/tracks/position - Add a track at a specific position
     * 
     * ENHANCED WITH @Valid: Comprehensive validation for position-based track insertion
     * - @Valid enforces trackId and position validation via annotations
     * - @NotBlank ensures trackId is provided and not empty
     * - @Min(0) ensures position is non-negative
     * - Domain layer (Playlist entity) handles position upper bound validation
     * - Automatic structured error response if validation fails
     * 
     * Returns 200 OK with the updated playlist
     * Throws InvalidTrackPositionException if position exceeds playlist bounds (handled by GlobalExceptionHandler)
     */
    @PostMapping("/{playlistId}/tracks/position")
    public ResponseEntity<Playlist> addTrackAtPosition(@PathVariable String playlistId, 
                                                       @Valid @RequestBody AddTrackAtPositionRequest request) {
        // Basic validation completed - trackId exists and position >= 0
        // Domain validation for position upper bounds handled by rich Playlist entity
        Playlist playlist = playlistService.addTrackAtPosition(playlistId, request.trackId(), request.position());
        return ResponseEntity.ok(playlist);
    }

    /**
     * POST /playlists/{playlistId}/tracks/multiple - Add multiple tracks to a playlist
     * 
     * ENHANCED WITH @Valid: Comprehensive validation for bulk track insertion
     * - @Valid enforces trackIds list and position validation via annotations
     * - @NotEmpty ensures trackIds list is provided and not empty
     * - @Min(0) ensures position is non-negative (if provided)
     * - Domain layer (Playlist entity) handles position upper bound validation
     * - Automatic structured error response if validation fails
     * 
     * Returns 200 OK with the updated playlist
     * Throws InvalidTrackPositionException if position exceeds playlist bounds (handled by GlobalExceptionHandler)
     * Throws TrackNotFoundException if any track ID is not found (handled by GlobalExceptionHandler)
     */
    @PostMapping("/{playlistId}/tracks/multiple")
    public ResponseEntity<Playlist> addMultipleTracks(@PathVariable String playlistId, 
                                                      @Valid @RequestBody AddMultipleTracksRequest request) {
        // Basic validation completed - trackIds list exists and position >= 0 (if provided)
        // Domain validation for position upper bounds and track existence handled by service and domain layers
        Playlist playlist = playlistService.addMultipleTracks(playlistId, request);
        return ResponseEntity.ok(playlist);
    }
}