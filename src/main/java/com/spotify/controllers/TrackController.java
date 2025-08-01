package com.spotify.controllers;

import com.spotify.domain.entities.Track;
import com.spotify.dto.CreateTrackRequest;
import com.spotify.dto.UpdateTrackRequest;
import com.spotify.services.TrackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for Track operations.
 * 
 * ENHANCED WITH AUTOMATIC VALIDATION: Now uses @Valid annotations to trigger
 * Jakarta Bean Validation on request DTOs before reaching service methods.
 * 
 * Benefits of @Valid integration:
 * - Automatic validation at API boundary
 * - Consistent error responses via GlobalExceptionHandler
 * - Reduced boilerplate validation code in controllers
 * - Clear separation: validation annotations define rules, @Valid triggers them
 * 
 * This improvement provides:
 * - Compile-time type safety (no more unsafe casting)
 * - Runtime validation via Jakarta Bean Validation
 * - Better IDE support with autocomplete and refactoring
 * - Clear API contracts through typed records
 * - Automatic validation before service layer execution
 * 
 * Follows REST API best practices with proper HTTP methods, status codes, and resource naming.
 */
@RestController
@RequestMapping("/tracks")
@Validated
public class TrackController {

    private final TrackService trackService;

    /**
     * Constructor-based dependency injection (Spring best practice).
     */
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * GET /tracks - Retrieve all tracks
     * Returns 200 OK with list of tracks
     */
    @GetMapping
    public ResponseEntity<List<Track>> getAllTracks() {
        List<Track> tracks = trackService.getAllTracks();
        return ResponseEntity.ok(tracks);
    }

    /**
     * GET /tracks/{id} - Retrieve a specific track by ID
     * Returns 200 OK if found, 404 NOT FOUND if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Track> getTrackById(@PathVariable String id) {
        Track track = trackService.getTrackById(id);
        return ResponseEntity.ok(track);
    }

    /**
     * POST /tracks - Create a new track using validated DTO
     * 
     * ENHANCED WITH @Valid: Automatic validation triggered before service call
     * - @Valid annotation tells Spring Boot to validate the request DTO
     * - Validation annotations in CreateTrackRequest are automatically enforced
     * - If validation fails, MethodArgumentNotValidException is thrown
     * - GlobalExceptionHandler catches and returns structured error response
     * 
     * Returns 201 CREATED with the created track
     */
    @PostMapping
    public ResponseEntity<Track> createTrack(@Valid @RequestBody CreateTrackRequest request) {
        // Validation completed successfully if we reach this point
        // Direct access to validated, typed fields - no casting required!
        Track track = trackService.createTrack(request.title(), request.artist(), request.duration());
        return ResponseEntity.status(HttpStatus.CREATED).body(track);
    }

    /**
     * PUT /tracks/{id} - Update an existing track using validated DTO
     * 
     * ENHANCED WITH @Valid: Automatic validation for update operations
     * - Jakarta Bean Validation annotations ensure data integrity
     * - @Valid triggers validation before service method execution
     * - Type-safe field access with automatic validation guarantees
     * 
     * Returns 200 OK with the updated track
     */
    @PutMapping("/{id}")
    public ResponseEntity<Track> updateTrack(@PathVariable String id, @Valid @RequestBody UpdateTrackRequest request) {
        // Validation completed - all fields meet annotation constraints
        Track track = trackService.updateTrack(id, request.title(), request.artist(), request.duration());
        return ResponseEntity.ok(track);
    }

    /**
     * DELETE /tracks/{id} - Delete a track
     * Returns 204 NO CONTENT when successfully deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable String id) {
        trackService.deleteTrack(id);
        return ResponseEntity.noContent().build();
    }
}