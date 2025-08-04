package com.spotify.unit.domain.entities;

import com.spotify.domain.entities.Track;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Track domain entity.
 * These tests focus on pure business logic without any Spring framework dependencies.
 * 
 * @Tag("unit") allows running only unit tests with: mvn test -Dtest="*Test" -Dgroups="unit"
 */
@Tag("unit")
@DisplayName("Track Domain Entity Tests")
class TrackTest {

    @Nested
    @DisplayName("Track Creation")
    class TrackCreation {

        @Test
        @DisplayName("Should create track with valid parameters")
        void shouldCreateTrackWithValidParameters() {
            // Given
            String title = "Bohemian Rhapsody";
            String artist = "Queen";
            Integer duration = 355; // 5 minutes 55 seconds

            // When
            Track track = new Track(title, artist, duration);

            // Then
            assertNotNull(track.getId(), "Track ID should be generated");
            assertEquals(title, track.getTitle());
            assertEquals(artist, track.getArtist());
            assertEquals(duration, track.getDuration());
            assertNull(track.getCreatedAt(), "CreatedAt should be null before persistence");
        }

        @Test
        @DisplayName("Should generate unique IDs for different tracks")
        void shouldGenerateUniqueIds() {
            // Given & When
            Track track1 = new Track("Song 1", "Artist 1", 200);
            Track track2 = new Track("Song 2", "Artist 2", 250);

            // Then
            assertNotEquals(track1.getId(), track2.getId(), "Track IDs should be unique");
        }

        @Test
        @DisplayName("Should have UUID-formatted ID")
        void shouldHaveUuidFormattedId() {
            // Given & When
            Track track = new Track("Test Song", "Test Artist", 180);

            // Then
            String id = track.getId();
            assertNotNull(id);
            assertEquals(36, id.length(), "UUID string should be 36 characters long");
            assertTrue(id.contains("-"), "UUID should contain hyphens");
        }
    }

    @Nested
    @DisplayName("Track Updates")
    class TrackUpdates {

        @Test
        @DisplayName("Should update title successfully")
        void shouldUpdateTitle() {
            // Given
            Track track = new Track("Original Title", "Artist", 200);
            String newTitle = "Updated Title";

            // When
            track.setTitle(newTitle);

            // Then
            assertEquals(newTitle, track.getTitle());
        }

        @Test
        @DisplayName("Should update artist successfully")
        void shouldUpdateArtist() {
            // Given
            Track track = new Track("Title", "Original Artist", 200);
            String newArtist = "Updated Artist";

            // When
            track.setArtist(newArtist);

            // Then
            assertEquals(newArtist, track.getArtist());
        }

        @Test
        @DisplayName("Should update duration successfully")
        void shouldUpdateDuration() {
            // Given
            Track track = new Track("Title", "Artist", 200);
            Integer newDuration = 300;

            // When
            track.setDuration(newDuration);

            // Then
            assertEquals(newDuration, track.getDuration());
        }
    }

    @Nested
    @DisplayName("Track Equality and HashCode")
    class TrackEqualityAndHashCode {

        @Test
        @DisplayName("Should be equal when IDs are the same")
        void shouldBeEqualWhenIdsAreSame() {
            // Given
            Track track1 = new Track("Song", "Artist", 200);
            Track track2 = new Track("Different Song", "Different Artist", 300);
            
            // Use reflection to set the same ID (simulating persistence scenario)
            try {
                java.lang.reflect.Field idField = Track.class.getDeclaredField("id");
                idField.setAccessible(true);
                String sameId = track1.getId();
                idField.set(track2, sameId);
            } catch (Exception e) {
                fail("Failed to set ID via reflection: " + e.getMessage());
            }

            // When & Then
            assertEquals(track1, track2, "Tracks with same ID should be equal");
            assertEquals(track1.hashCode(), track2.hashCode(), "Tracks with same ID should have same hashcode");
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Given
            Track track1 = new Track("Song", "Artist", 200);
            Track track2 = new Track("Song", "Artist", 200);

            // When & Then
            assertNotEquals(track1, track2, "Tracks with different IDs should not be equal");
        }

        @Test
        @DisplayName("Should not be equal to null or different class")
        void shouldNotBeEqualToNullOrDifferentClass() {
            // Given
            Track track = new Track("Song", "Artist", 200);

            // When & Then
            assertNotEquals(track, null, "Track should not equal null");
            assertNotEquals(track, "Not a track", "Track should not equal different class");
        }
    }

    @Nested
    @DisplayName("Track String Representation")
    class TrackStringRepresentation {

        @Test
        @DisplayName("Should include all important fields in toString")
        void shouldIncludeAllImportantFieldsInToString() {
            // Given
            Track track = new Track("Bohemian Rhapsody", "Queen", 355);

            // When
            String trackString = track.toString();

            // Then
            assertAll("toString should contain all fields",
                () -> assertTrue(trackString.contains(track.getId()), "Should contain ID"),
                () -> assertTrue(trackString.contains("Bohemian Rhapsody"), "Should contain title"),
                () -> assertTrue(trackString.contains("Queen"), "Should contain artist"),
                () -> assertTrue(trackString.contains("355"), "Should contain duration"),
                () -> assertTrue(trackString.contains("Track{"), "Should start with class name")
            );
        }
    }
}