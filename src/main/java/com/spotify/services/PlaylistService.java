package com.spotify.services;

import com.spotify.domain.entities.Playlist;
import com.spotify.domain.entities.Track;
import com.spotify.domain.exceptions.PlaylistNotFoundException;
import com.spotify.repositories.PlaylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application service for Playlist operations.
 * Following DDD principles, this service remains thin and delegates
 * business logic to the rich Playlist domain entity. The actual business
 * rules for track management are encapsulated within the Playlist entity itself.
 */
@Service
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackService trackService;

    /**
     * Constructor-based dependency injection (Spring best practice).
     */
    public PlaylistService(PlaylistRepository playlistRepository, TrackService trackService) {
        this.playlistRepository = playlistRepository;
        this.trackService = trackService;
    }

    /**
     * Create a new playlist.
     * The Playlist entity handles ID generation and initialization.
     */
    public Playlist createPlaylist(String name, Boolean isPublic) {
        Playlist playlist = new Playlist(name, isPublic);
        return playlistRepository.save(playlist);
    }

    /**
     * Retrieve all playlists.
     */
    @Transactional(readOnly = true)
    public List<Playlist> getAllPlaylists() {
        return playlistRepository.findAll();
    }

    /**
     * Find a playlist by ID.
     * Throws domain exception if not found.
     */
    @Transactional(readOnly = true)
    public Playlist getPlaylistById(String id) {
        return playlistRepository.findById(id)
                .orElseThrow(() -> new PlaylistNotFoundException(id));
    }

    /**
     * Update an existing playlist.
     */
    public Playlist updatePlaylist(String id, String name, Boolean isPublic) {
        Playlist playlist = getPlaylistById(id);
        playlist.setName(name);
        playlist.setIsPublic(isPublic);
        return playlistRepository.save(playlist);
    }

    /**
     * Delete a playlist by ID.
     */
    public void deletePlaylist(String id) {
        Playlist playlist = getPlaylistById(id);
        playlistRepository.delete(playlist);
    }

    /**
     * Add a track to the end of a playlist.
     * Delegates to the rich domain method in Playlist entity.
     */
    public Playlist addTrackToPlaylist(String playlistId, String trackId) {
        Playlist playlist = getPlaylistById(playlistId);
        Track track = trackService.getTrackById(trackId);
        
        // Delegate to domain entity business logic
        playlist.addTrack(track);
        
        return playlistRepository.save(playlist);
    }

    /**
     * Add a track at a specific position in a playlist.
     * Delegates to the rich domain method in Playlist entity.
     */
    public Playlist addTrackAtPosition(String playlistId, String trackId, int position) {
        Playlist playlist = getPlaylistById(playlistId);
        Track track = trackService.getTrackById(trackId);
        
        // Delegate to domain entity business logic that enforces position validation
        playlist.addTrackAtPosition(track, position);
        
        return playlistRepository.save(playlist);
    }

    /**
     * Get all tracks in a playlist.
     * The Playlist entity protects its internal track collection.
     */
    @Transactional(readOnly = true)
    public List<Track> getPlaylistTracks(String playlistId) {
        Playlist playlist = getPlaylistById(playlistId);
        return playlist.getTracks(); // Returns defensive copy
    }
}