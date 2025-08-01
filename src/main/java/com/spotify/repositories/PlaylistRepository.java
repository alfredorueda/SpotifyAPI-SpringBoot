package com.spotify.repositories;

import com.spotify.domain.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Playlist entity data access.
 * Extends JpaRepository to provide standard CRUD operations.
 * Spring Data JPA will automatically implement this interface.
 */
@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {
    // Additional custom query methods can be added here if needed
}