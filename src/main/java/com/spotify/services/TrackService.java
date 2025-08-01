package com.spotify.services;

import com.spotify.domain.entities.Track;
import com.spotify.domain.exceptions.TrackNotFoundException;
import com.spotify.repositories.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for Track operations.
 * Following DDD principles, this service layer remains thin and delegates
 * business logic to the rich domain entities. It primarily coordinates
 * between the presentation layer and the domain/persistence layers.
 */
@Service
@Transactional
public class TrackService {

    private final TrackRepository trackRepository;

    /**
     * Constructor-based dependency injection (Spring best practice).
     */
    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    /**
     * Create a new track.
     * The Track entity handles ID generation and business logic.
     */
    public Track createTrack(String title, String artist, Integer duration) {
        Track track = new Track(title, artist, duration);
        return trackRepository.save(track);
    }

    /**
     * Retrieve all tracks.
     */
    @Transactional(readOnly = true)
    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    /**
     * Find a track by ID.
     * Throws domain exception if not found.
     */
    @Transactional(readOnly = true)
    public Track getTrackById(String id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new TrackNotFoundException(id));
    }

    /**
     * Update an existing track.
     */
    public Track updateTrack(String id, String title, String artist, Integer duration) {
        Track track = getTrackById(id);
        track.setTitle(title);
        track.setArtist(artist);
        track.setDuration(duration);
        return trackRepository.save(track);
    }

    /**
     * Delete a track by ID.
     */
    public void deleteTrack(String id) {
        Track track = getTrackById(id);
        trackRepository.delete(track);
    }
}