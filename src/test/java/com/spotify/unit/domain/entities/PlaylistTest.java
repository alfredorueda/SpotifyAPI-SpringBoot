package com.spotify.unit.domain.entities;

import com.spotify.domain.entities.Playlist;
import com.spotify.domain.entities.Track;
import com.spotify.domain.exceptions.InvalidTrackPositionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Playlist domain entity.
 * These tests focus on the rich domain behavior and business logic
 * without any Spring framework dependencies.
 * 
 * @Tag("unit") allows running only unit tests with: mvn test -Dtest="*Test" -Dgroups="unit"
 */
@Tag("unit")
@DisplayName("Playlist Domain Entity Tests")
class PlaylistTest {

    private Track track1;
    private Track track2;
    private Track track3;

    @BeforeEach
    void setUp() {
        // Create sample tracks for testing
        track1 = new Track("Bohemian Rhapsody", "Queen", 355);
        track2 = new Track("Stairway to Heaven", "Led Zeppelin", 482);
        track3 = new Track("Hotel California", "Eagles", 391);
    }

    @Nested
    @DisplayName("Playlist Creation")
    class PlaylistCreation {

        @Test
        @DisplayName("Should create playlist with valid parameters")
        void shouldCreatePlaylistWithValidParameters() {
            // Given
            String name = "My Awesome Playlist";
            Boolean isPublic = true;

            // When
            Playlist playlist = new Playlist(name, isPublic);

            // Then
            assertNotNull(playlist.getId(), "Playlist ID should be generated");
            assertEquals(name, playlist.getName());
            assertEquals(isPublic, playlist.getIsPublic());
            assertTrue(playlist.getTracks().isEmpty(), "New playlist should have no tracks");
            assertEquals(0, playlist.getTrackCount(), "Track count should be 0");
            assertNull(playlist.getCreatedAt(), "CreatedAt should be null before persistence");
        }

        @Test
        @DisplayName("Should generate unique IDs for different playlists")
        void shouldGenerateUniqueIds() {
            // Given & When
            Playlist playlist1 = new Playlist("Playlist 1", true);
            Playlist playlist2 = new Playlist("Playlist 2", false);

            // Then
            assertNotEquals(playlist1.getId(), playlist2.getId(), "Playlist IDs should be unique");
        }

        @Test
        @DisplayName("Should create private playlist")
        void shouldCreatePrivatePlaylist() {
            // Given & When
            Playlist playlist = new Playlist("Private Playlist", false);

            // Then
            assertFalse(playlist.getIsPublic(), "Playlist should be private");
        }
    }

    @Nested
    @DisplayName("Adding Single Tracks")
    class AddingSingleTracks {

        @Test
        @DisplayName("Should add track to empty playlist")
        void shouldAddTrackToEmptyPlaylist() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);

            // When
            playlist.addTrack(track1);

            // Then
            assertEquals(1, playlist.getTrackCount());
            assertTrue(playlist.getTracks().contains(track1));
        }

        @Test
        @DisplayName("Should add multiple different tracks")
        void shouldAddMultipleDifferentTracks() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);

            // When
            playlist.addTrack(track1);
            playlist.addTrack(track2);
            playlist.addTrack(track3);

            // Then
            assertEquals(3, playlist.getTrackCount());
            List<Track> tracks = playlist.getTracks();
            assertEquals(track1, tracks.get(0));
            assertEquals(track2, tracks.get(1));
            assertEquals(track3, tracks.get(2));
        }

        @Test
        @DisplayName("Should not add duplicate tracks")
        void shouldNotAddDuplicateTracks() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);

            // When
            playlist.addTrack(track1); // Try to add same track again

            // Then
            assertEquals(1, playlist.getTrackCount(), "Should not add duplicate track");
        }

        @Test
        @DisplayName("Should throw exception when adding null track")
        void shouldThrowExceptionWhenAddingNullTrack() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);

            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> playlist.addTrack(null)
            );
            assertEquals("Track cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Adding Tracks at Position")
    class AddingTracksAtPosition {

        @Test
        @DisplayName("Should add track at beginning of playlist")
        void shouldAddTrackAtBeginning() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track2);
            playlist.addTrack(track3);

            // When
            playlist.addTrackAtPosition(track1, 0);

            // Then
            List<Track> tracks = playlist.getTracks();
            assertEquals(3, playlist.getTrackCount());
            assertEquals(track1, tracks.get(0));
            assertEquals(track2, tracks.get(1));
            assertEquals(track3, tracks.get(2));
        }

        @Test
        @DisplayName("Should add track in middle of playlist")
        void shouldAddTrackInMiddle() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);
            playlist.addTrack(track3);

            // When
            playlist.addTrackAtPosition(track2, 1);

            // Then
            List<Track> tracks = playlist.getTracks();
            assertEquals(3, playlist.getTrackCount());
            assertEquals(track1, tracks.get(0));
            assertEquals(track2, tracks.get(1));
            assertEquals(track3, tracks.get(2));
        }

        @Test
        @DisplayName("Should add track at end of playlist")
        void shouldAddTrackAtEnd() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);
            playlist.addTrack(track2);

            // When
            playlist.addTrackAtPosition(track3, 2);

            // Then
            List<Track> tracks = playlist.getTracks();
            assertEquals(3, playlist.getTrackCount());
            assertEquals(track3, tracks.get(2));
        }

        @Test
        @DisplayName("Should throw exception for invalid position")
        void shouldThrowExceptionForInvalidPosition() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);

            // When & Then
            InvalidTrackPositionException exception = assertThrows(
                InvalidTrackPositionException.class,
                () -> playlist.addTrackAtPosition(track2, 5)
            );
            assertTrue(exception.getMessage().contains("Invalid track position: 5"));
        }

        @Test
        @DisplayName("Should throw exception for negative position")
        void shouldThrowExceptionForNegativePosition() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);

            // When & Then
            InvalidTrackPositionException exception = assertThrows(
                InvalidTrackPositionException.class,
                () -> playlist.addTrackAtPosition(track1, -1)
            );
            assertTrue(exception.getMessage().contains("Invalid track position: -1"));
        }
    }

    @Nested
    @DisplayName("Adding Multiple Tracks")
    class AddingMultipleTracks {

        @Test
        @DisplayName("Should add multiple tracks at end when position is null")
        void shouldAddMultipleTracksAtEndWhenPositionIsNull() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);
            List<Track> newTracks = Arrays.asList(track2, track3);

            // When
            playlist.addTracks(newTracks, null);

            // Then
            List<Track> tracks = playlist.getTracks();
            assertEquals(3, playlist.getTrackCount());
            assertEquals(track1, tracks.get(0));
            assertEquals(track2, tracks.get(1));
            assertEquals(track3, tracks.get(2));
        }

        @Test
        @DisplayName("Should add multiple tracks at specified position")
        void shouldAddMultipleTracksAtSpecifiedPosition() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);
            List<Track> newTracks = Arrays.asList(track2, track3);

            // When
            playlist.addTracks(newTracks, 0);

            // Then
            List<Track> tracks = playlist.getTracks();
            assertEquals(3, playlist.getTrackCount());
            assertEquals(track2, tracks.get(0));
            assertEquals(track3, tracks.get(1));
            assertEquals(track1, tracks.get(2));
        }

        @Test
        @DisplayName("Should filter out null tracks when adding multiple")
        void shouldFilterOutNullTracksWhenAddingMultiple() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            List<Track> tracksWithNull = Arrays.asList(track1, null, track2);

            // When
            playlist.addTracks(tracksWithNull, null);

            // Then
            assertEquals(2, playlist.getTrackCount(), "Should ignore null tracks");
            List<Track> tracks = playlist.getTracks();
            assertEquals(track1, tracks.get(0));
            assertEquals(track2, tracks.get(1));
        }

        @Test
        @DisplayName("Should filter out duplicate tracks when adding multiple")
        void shouldFilterOutDuplicateTracksWhenAddingMultiple() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);
            List<Track> tracksWithDuplicate = Arrays.asList(track1, track2);

            // When
            playlist.addTracks(tracksWithDuplicate, null);

            // Then
            assertEquals(2, playlist.getTrackCount(), "Should ignore duplicate tracks");
            List<Track> tracks = playlist.getTracks();
            assertEquals(track1, tracks.get(0));
            assertEquals(track2, tracks.get(1));
        }

        @Test
        @DisplayName("Should throw exception for invalid position when adding multiple")
        void shouldThrowExceptionForInvalidPositionWhenAddingMultiple() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            List<Track> newTracks = Arrays.asList(track1, track2);

            // When & Then
            InvalidTrackPositionException exception = assertThrows(
                InvalidTrackPositionException.class,
                () -> playlist.addTracks(newTracks, 5)
            );
            assertTrue(exception.getMessage().contains("Invalid track position: 5"));
        }

        @Test
        @DisplayName("Should throw exception for null or empty tracks list")
        void shouldThrowExceptionForNullOrEmptyTracksList() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);

            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> playlist.addTracks(null, 0)
            );
            
            assertThrows(
                IllegalArgumentException.class,
                () -> playlist.addTracks(Arrays.asList(), 0)
            );
        }
    }

    @Nested
    @DisplayName("Removing Tracks")
    class RemovingTracks {

        @Test
        @DisplayName("Should remove existing track")
        void shouldRemoveExistingTrack() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);
            playlist.addTrack(track2);

            // When
            boolean removed = playlist.removeTrack(track1);

            // Then
            assertTrue(removed, "Should return true when track is removed");
            assertEquals(1, playlist.getTrackCount());
            assertFalse(playlist.getTracks().contains(track1));
            assertTrue(playlist.getTracks().contains(track2));
        }

        @Test
        @DisplayName("Should return false when removing non-existent track")
        void shouldReturnFalseWhenRemovingNonExistentTrack() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);

            // When
            boolean removed = playlist.removeTrack(track2);

            // Then
            assertFalse(removed, "Should return false when track doesn't exist");
            assertEquals(1, playlist.getTrackCount());
        }
    }

    @Nested
    @DisplayName("Playlist Calculations")
    class PlaylistCalculations {

        @Test
        @DisplayName("Should calculate total duration correctly")
        void shouldCalculateTotalDurationCorrectly() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1); // 355 seconds
            playlist.addTrack(track2); // 482 seconds
            playlist.addTrack(track3); // 391 seconds

            // When
            int totalDuration = playlist.getTotalDuration();

            // Then
            assertEquals(1228, totalDuration, "Total duration should be sum of all track durations");
        }

        @Test
        @DisplayName("Should return zero duration for empty playlist")
        void shouldReturnZeroDurationForEmptyPlaylist() {
            // Given
            Playlist playlist = new Playlist("Empty Playlist", true);

            // When
            int totalDuration = playlist.getTotalDuration();

            // Then
            assertEquals(0, totalDuration, "Empty playlist should have zero duration");
        }
    }

    @Nested
    @DisplayName("Playlist Updates")
    class PlaylistUpdates {

        @Test
        @DisplayName("Should update playlist name")
        void shouldUpdatePlaylistName() {
            // Given
            Playlist playlist = new Playlist("Original Name", true);
            String newName = "Updated Name";

            // When
            playlist.setName(newName);

            // Then
            assertEquals(newName, playlist.getName());
        }

        @Test
        @DisplayName("Should update playlist visibility")
        void shouldUpdatePlaylistVisibility() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);

            // When
            playlist.setIsPublic(false);

            // Then
            assertFalse(playlist.getIsPublic());
        }
    }

    @Nested
    @DisplayName("Tracks List Encapsulation")
    class TracksListEncapsulation {

        @Test
        @DisplayName("Should return defensive copy of tracks list")
        void shouldReturnDefensiveCopyOfTracksList() {
            // Given
            Playlist playlist = new Playlist("Test Playlist", true);
            playlist.addTrack(track1);

            // When
            List<Track> tracks = playlist.getTracks();
            
            // Try to modify the returned list
            tracks.add(track2);

            // Then
            assertEquals(1, playlist.getTrackCount(), "Original playlist should not be modified");
            assertFalse(playlist.getTracks().contains(track2), "Playlist should not contain the externally added track");
        }
    }
}
