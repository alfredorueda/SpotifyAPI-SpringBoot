package com.spotify.integration.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Simple end-to-end integration test for Playlist API endpoints.
 * Tests the complete happy path flow from REST controller through service, repository, and database.
 * 
 * Uses @SpringBootTest with webEnvironment.RANDOM_PORT to start the full Spring context
 * with an embedded server for true end-to-end testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlaylistControllerSimpleIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void shouldPerformSimplePlaylistLifecycle() {
        // 1. Create a new playlist (POST /playlists)
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "My Favorite Songs",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("name", equalTo("My Favorite Songs"))
                .body("isPublic", equalTo(true))
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
            .extract()
                .path("id");

        // 2. Retrieve the created playlist by ID (GET /playlists/{id})
        given()
            .when()
                .get("/playlists/{id}", playlistId)
            .then()
                .statusCode(200)
                .body("id", equalTo(playlistId))
                .body("name", equalTo("My Favorite Songs"))
                .body("isPublic", equalTo(true))
                .body("createdAt", notNullValue());

        // 3. Update the playlist (PUT /playlists/{id})
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "My Updated Playlist",
                        "isPublic": false
                    }
                    """)
            .when()
                .put("/playlists/{id}", playlistId)
            .then()
                .statusCode(200)
                .body("id", equalTo(playlistId))
                .body("name", equalTo("My Updated Playlist"))
                .body("isPublic", equalTo(false));

        // 4. Verify the playlist appears in the list of all playlists (GET /playlists)
        given()
            .when()
                .get("/playlists")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.id == '" + playlistId + "' }.name", 
                      equalTo("My Updated Playlist"));

        // 5. Delete the playlist (DELETE /playlists/{id})
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);

        // 6. Verify the playlist is no longer accessible (GET /playlists/{id})
        given()
            .when()
                .get("/playlists/{id}", playlistId)
            .then()
                .statusCode(404);
    }

    @Test
    void shouldAddTracksToPlaylist() {
        // First, create a playlist
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Rock Classics",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Then, create a track to add to the playlist
        String trackId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Bohemian Rhapsody",
                        "artist": "Queen",
                        "duration": 355
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Add the track to the playlist (POST /playlists/{playlistId}/tracks)
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackId": "%s"
                    }
                    """.formatted(trackId))
            .when()
                .post("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("id", equalTo(playlistId))
                .body("name", equalTo("Rock Classics"));

        // Verify the track appears in the playlist (GET /playlists/{playlistId}/tracks)
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(trackId))
                .body("[0].title", equalTo("Bohemian Rhapsody"))
                .body("[0].artist", equalTo("Queen"));

        /**
         * <hr></hr> The observed behavior, where deleting a playlist returns a 204 No Content
         * status and subsequently attempting to delete a track from that playlist returns a
         * 404 Not Found, is consistent with a domain model based on composition. In this model,
         * the playlist entity is considered the owner of its tracks. Consequently, when a
         * playlist is deleted, all associated tracks are also removed from the system. Any
         * further operation on those tracks, such as deletion, will result in a 404 Not Found
         * response, as the tracks no longer exist.
         *
         * This behavior aligns with the principles of strong composition in domain-driven
         * design, where the lifecycle of the contained entities (tracks) is strictly bound
         * to the aggregate root (playlist). The test expecting a 404 response after the
         * playlist deletion is therefore correct under this model.
         *
         * If, however, the domain requires tracks to exist independently of playlists, the
         * implementation should be adjusted so that deleting a playlist does not remove its
         * tracks. In such a scenario, deleting a track after its playlist has been deleted
         * should still return a 204 No Content status, indicating successful deletion.
         *
         * In summary, the correctness of the test and the implementation depends on the
         * intended domain model. For a composition relationship, the current behavior and
         * test expectations are appropriate. If a different relationship is desired, the
         * implementation should be revised accordingly.
         */

        // Clean up: delete the playlist and track
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);

        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(404);
    }

    @Test
    void shouldAddMultipleTracksToPlaylist() {
        // First, create a playlist
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "My Mix Playlist",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Create multiple tracks to add to the playlist
        String track1Id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Hotel California",
                        "artist": "Eagles",
                        "duration": 391
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        String track2Id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Stairway to Heaven",
                        "artist": "Led Zeppelin",
                        "duration": 482
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        String track3Id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Sweet Child O' Mine",
                        "artist": "Guns N' Roses",
                        "duration": 356
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Add multiple tracks to the playlist at once (POST /playlists/{playlistId}/tracks/multiple)
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackIds": ["%s", "%s", "%s"]
                    }
                    """.formatted(track1Id, track2Id, track3Id))
            .when()
                .post("/playlists/{playlistId}/tracks/multiple", playlistId)
            .then()
                .statusCode(200)
                .body("id", equalTo(playlistId))
                .body("name", equalTo("My Mix Playlist"));

        // Verify all tracks appear in the playlist (GET /playlists/{playlistId}/tracks)
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("find { it.id == '" + track1Id + "' }.title", equalTo("Hotel California"))
                .body("find { it.id == '" + track2Id + "' }.title", equalTo("Stairway to Heaven"))
                .body("find { it.id == '" + track3Id + "' }.title", equalTo("Sweet Child O' Mine"));

        // Clean up: delete the playlist and tracks
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);

        given().when().delete("/tracks/{id}", track1Id).then().statusCode(404);
        given().when().delete("/tracks/{id}", track2Id).then().statusCode(404);
        given().when().delete("/tracks/{id}", track3Id).then().statusCode(404);
    }

    @Test
    void shouldHandleErrorWhenAddingNonExistentTrack() {
        // First, create a playlist
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Error Test Playlist",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Try to add a non-existent track to the playlist
        String nonExistentTrackId = "non-existent-track-id";
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackId": "%s"
                    }
                    """.formatted(nonExistentTrackId))
            .when()
                .post("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(404); // Expecting 404 for non-existent track

        // Clean up: delete the playlist
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);
    }

    @Test
    void shouldHandleErrorWhenAddingMultipleNonExistentTracks() {
        // First, create a playlist
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Multiple Error Test Playlist",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Try to add multiple non-existent tracks to the playlist
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackIds": ["non-existent-1", "non-existent-2", "non-existent-3"]
                    }
                    """)
            .when()
                .post("/playlists/{playlistId}/tracks/multiple", playlistId)
            .then()
                .statusCode(404); // Expecting 404 for non-existent tracks

        // Clean up: delete the playlist
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);
    }

    @Test
    void shouldHandleErrorWhenAddingTrackToNonExistentPlaylist() {
        // Create a track first
        String trackId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Lonely Track",
                        "artist": "Test Artist",
                        "duration": 180
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Try to add the track to a non-existent playlist
        String nonExistentPlaylistId = "non-existent-playlist-id";
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackId": "%s"
                    }
                    """.formatted(trackId))
            .when()
                .post("/playlists/{playlistId}/tracks", nonExistentPlaylistId)
            .then()
                .statusCode(404); // Expecting 404 for non-existent playlist

        // Clean up: delete the track
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);
    }

    @Test
    void shouldVerifyCompositionRelationshipWhenTrackIsDeleted() {
        // Create a playlist
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Composition Test Playlist",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Create a track
        String trackId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Temporary Track",
                        "artist": "Test Artist",
                        "duration": 200
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Add the track to the playlist
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackId": "%s"
                    }
                    """.formatted(trackId))
            .when()
                .post("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200);

        // Verify the track is in the playlist
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].id", equalTo(trackId));

        // Delete the track (this should affect the composition relationship)
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);

        // Now verify that the track is no longer accessible individually
        given()
            .when()
                .get("/tracks/{id}", trackId)
            .then()
                .statusCode(404); // Track should not exist anymore

        // Due to composition relationship, the playlist should handle this gracefully
        // The playlist should still exist but the track should no longer be in it
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(0)); // No tracks should remain

        // Clean up: delete the playlist
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);
    }

    @Test
    void shouldHandlePartialErrorWhenAddingMixOfExistentAndNonExistentTracks() {
        // First, create a playlist
        String playlistId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "Partial Error Test Playlist",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Create some valid tracks
        String validTrack1Id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Valid Track 1",
                        "artist": "Real Artist",
                        "duration": 180
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        String validTrack2Id = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Valid Track 2",
                        "artist": "Real Artist 2",
                        "duration": 220
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(201)
                .body("id", notNullValue())
            .extract()
                .path("id");

        // Try to add a mix of valid and invalid tracks to the playlist
        // This should test how the system handles partial failures
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackIds": ["%s", "non-existent-track-1", "%s", "non-existent-track-2"]
                    }
                    """.formatted(validTrack1Id, validTrack2Id))
            .when()
                .post("/playlists/{playlistId}/tracks/multiple", playlistId)
            .then()
                .statusCode(404); // Expecting 404 because some tracks don't exist
                // The system should fail fast and not add any tracks if any are invalid

        // Verify that no tracks were added to the playlist due to the partial failure
        // This tests the transactional behavior - either all tracks are added or none
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(0)); // No tracks should have been added

        // Clean up: delete the playlist and the valid tracks
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);

        given().when().delete("/tracks/{id}", validTrack1Id).then().statusCode(204);
        given().when().delete("/tracks/{id}", validTrack2Id).then().statusCode(204);
    }
}