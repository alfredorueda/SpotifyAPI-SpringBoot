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
 * ADVANCED INTEGRATION TESTS FOR PLAYLIST API WITH TRACK RELATIONSHIPS
 * 
 * This comprehensive test class demonstrates advanced Spring Boot integration testing
 * patterns for junior developers learning complex REST API testing scenarios.
 * 
 * EDUCATIONAL FOCUS AREAS:
 * 
 * 1. COMPLEX RESOURCE RELATIONSHIPS
 *    - One-to-Many relationships (Playlist → Tracks)
 *    - Composition vs Aggregation patterns
 *    - Cascade operations and lifecycle management
 *    - Referential integrity testing
 * 
 * 2. ADVANCED ERROR HANDLING SCENARIOS
 *    - Partial failure handling in batch operations
 *    - Transactional behavior verification
 *    - Cross-resource validation (playlist + track existence)
 *    - Concurrent operation safety
 * 
 * 3. REAL-WORLD API PATTERNS
 *    - Resource creation and linking workflows
 *    - Bulk operations and their error semantics
 *    - State consistency across related entities
 *    - Proper cleanup and resource management
 * 
 * 4. SPRING BOOT TESTING BEST PRACTICES
 *    - Test isolation and cleanup strategies
 *    - Database state management in tests
 *    - Integration test structure and organization
 *    - Error scenario coverage and validation
 * 
 * COMPOSITION RELATIONSHIP EXPLAINED:
 * In this system, Playlist and Track have a composition relationship, meaning:
 * - Tracks are "owned" by playlists (strong ownership)
 * - When a playlist is deleted, its tracks are also removed
 * - Tracks cannot exist without being part of a playlist
 * - This affects error handling and deletion semantics
 * 
 * This is different from aggregation where tracks would exist independently.
 * Understanding this relationship is crucial for proper API design and testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PlaylistControllerSimpleIntegrationTest {

    /**
     * Spring Boot automatically injects the random port number.
     * Using random ports prevents conflicts when running tests in parallel
     * or in CI/CD environments where multiple test suites run simultaneously.
     */
    @LocalServerPort
    private int port;

    /**
     * SETUP CONFIGURATION FOR EACH TEST
     * 
     * The @BeforeEach annotation ensures this method runs before every test,
     * providing clean state and consistent configuration. This is crucial for:
     * - Test isolation (each test starts with known state)
     * - Preventing test interdependencies
     * - Consistent HTTP client configuration
     */
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    /**
     * COMPLETE PLAYLIST LIFECYCLE TEST - FOUNDATION PATTERN
     * 
     * This test demonstrates the fundamental CRUD operations for a REST resource.
     * Every REST API should support this basic lifecycle pattern.
     * 
     * LEARNING OBJECTIVES:
     * - REST API design patterns and conventions
     * - HTTP status code semantics and proper usage
     * - Resource state transitions and persistence
     * - JSON serialization/deserialization verification
     * - Database integration and data consistency
     * 
     * WORKFLOW TESTED:
     * CREATE → READ → UPDATE → LIST → DELETE → VERIFY DELETION
     * 
     * This pattern should work for any well-designed REST resource.
     */
    @Test
    void shouldPerformSimplePlaylistLifecycle() {
        // STEP 1: CREATE A NEW PLAYLIST
        // POST /playlists - Creates a new playlist resource
        // Expected: 201 Created with resource data and auto-generated fields
        String playlistId = given()
                .contentType(ContentType.JSON)  // Specify request content type
                .body("""
                    {
                        "name": "My Favorite Songs",
                        "isPublic": true
                    }
                    """)
            .when()
                .post("/playlists")  // HTTP POST for resource creation
            .then()
                .statusCode(201)  // 201 Created indicates successful resource creation
                .body("name", equalTo("My Favorite Songs"))      // Verify request data persisted
                .body("isPublic", equalTo(true))
                .body("id", notNullValue())                      // Server generates unique ID
                .body("createdAt", notNullValue())               // Server sets creation timestamp
            .extract()
                .path("id");  // Extract ID for subsequent operations

        // STEP 2: RETRIEVE THE CREATED PLAYLIST BY ID
        // GET /playlists/{id} - Retrieves specific playlist by ID
        // Expected: 200 OK with complete resource data
        given()
            .when()
                .get("/playlists/{id}", playlistId)  // Path parameter substitution
            .then()
                .statusCode(200)  // 200 OK for successful retrieval
                .body("id", equalTo(playlistId))             // Verify correct resource returned
                .body("name", equalTo("My Favorite Songs"))
                .body("isPublic", equalTo(true))
                .body("createdAt", notNullValue());          // Verify persistence of timestamps

        // STEP 3: UPDATE THE PLAYLIST
        // PUT /playlists/{id} - Complete resource replacement
        // Expected: 200 OK with updated resource data
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "name": "My Updated Playlist",
                        "isPublic": false
                    }
                    """)
            .when()
                .put("/playlists/{id}", playlistId)  // HTTP PUT for complete update
            .then()
                .statusCode(200)  // 200 OK for successful update
                .body("id", equalTo(playlistId))             // ID remains unchanged
                .body("name", equalTo("My Updated Playlist")) // Updates applied
                .body("isPublic", equalTo(false));

        // STEP 4: VERIFY PLAYLIST APPEARS IN COLLECTION
        // GET /playlists - Retrieves all playlists
        // Expected: 200 OK with array containing our playlist
        given()
            .when()
                .get("/playlists")  // Collection endpoint
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))  // At least our playlist exists
                .body("find { it.id == '" + playlistId + "' }.name", 
                      equalTo("My Updated Playlist"));        // Verify our playlist in collection

        // STEP 5: DELETE THE PLAYLIST
        // DELETE /playlists/{id} - Removes the resource
        // Expected: 204 No Content (successful deletion, no response body)
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);  // 204 No Content indicates successful deletion

        // STEP 6: VERIFY DELETION COMPLETED
        // GET /playlists/{id} - Attempt to retrieve deleted resource
        // Expected: 404 Not Found (resource no longer exists)
        given()
            .when()
                .get("/playlists/{id}", playlistId)
            .then()
                .statusCode(404);  // 404 Not Found confirms deletion
    }

    /**
     * BASIC TRACK-TO-PLAYLIST RELATIONSHIP TEST
     * 
     * This test introduces the concept of resource relationships in REST APIs.
     * It demonstrates how to work with related resources and manage their lifecycle.
     * 
     * LEARNING OBJECTIVES:
     * - Resource relationship management in REST APIs
     * - Sub-resource endpoints (nested resource access)
     * - Composition relationship behavior and implications
     * - Cross-resource operations and their semantics
     * - Resource cleanup and cascade deletion patterns
     * 
     * RELATIONSHIP PATTERN DEMONSTRATED:
     * Playlist (1) ← contains → (N) Tracks
     * 
     * This is a composition relationship where tracks are owned by playlists.
     */
    @Test
    void shouldAddTracksToPlaylist() {
        // STEP 1: CREATE A PLAYLIST (PARENT RESOURCE)
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

        // STEP 2: CREATE A TRACK (CHILD RESOURCE)
        // Note: In this system, tracks can be created independently first,
        // then associated with playlists through a separate operation
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

        // STEP 3: ADD TRACK TO PLAYLIST (ESTABLISH RELATIONSHIP)
        // POST /playlists/{playlistId}/tracks - Adds a track to a specific playlist
        // This creates the relationship between the two resources
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "trackId": "%s"
                    }
                    """.formatted(trackId))
            .when()
                .post("/playlists/{playlistId}/tracks", playlistId)  // Sub-resource endpoint
            .then()
                .statusCode(200)  // 200 OK for successful relationship creation
                .body("id", equalTo(playlistId))
                .body("name", equalTo("Rock Classics"));

        // STEP 4: VERIFY TRACK APPEARS IN PLAYLIST
        // GET /playlists/{playlistId}/tracks - Retrieves tracks within a playlist
        // This demonstrates sub-resource access patterns
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(1))                   // One track in playlist
                .body("[0].id", equalTo(trackId))             // Correct track included
                .body("[0].title", equalTo("Bohemian Rhapsody"))
                .body("[0].artist", equalTo("Queen"));

        /**
         * COMPOSITION RELATIONSHIP BEHAVIOR DEMONSTRATION
         * 
         * The following cleanup section demonstrates the composition relationship
         * between playlists and tracks. In a composition relationship:
         * 
         * - The parent (playlist) owns the children (tracks)
         * - Deleting the parent also removes the children
         * - Children cannot exist independently after parent deletion
         * 
         * This is different from aggregation where children would remain
         * independent after parent deletion.
         */

        // STEP 5: DELETE THE PLAYLIST (PARENT RESOURCE)
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);  // Successful deletion

        // STEP 6: VERIFY COMPOSITION RELATIONSHIP EFFECT
        // Due to composition relationship, deleting the playlist should also
        // remove its tracks. Attempting to delete the track should return 404
        // because it no longer exists (was deleted with the playlist).
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(404);  // Track no longer exists due to composition
    }

    /**
     * BULK OPERATIONS TEST - MULTIPLE TRACKS TO PLAYLIST
     * 
     * This test demonstrates bulk operations and their importance in real-world APIs.
     * Bulk operations improve performance and provide better user experience.
     * 
     * LEARNING OBJECTIVES:
     * - Bulk operation design patterns
     * - Performance considerations in API design
     * - Batch request/response handling
     * - All-or-nothing operation semantics
     * - Error handling in bulk operations
     */
    @Test
    void shouldAddMultipleTracksToPlaylist() {
        // Create a playlist for bulk operations testing
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

        // Create multiple tracks to demonstrate bulk operations
        // In real applications, you might create tracks in bulk too
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

        // BULK OPERATION: Add multiple tracks at once
        // POST /playlists/{playlistId}/tracks/multiple - Bulk track addition
        // This is more efficient than multiple individual requests
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
                .statusCode(200)  // 200 OK for successful bulk operation
                .body("id", equalTo(playlistId))
                .body("name", equalTo("My Mix Playlist"));

        // VERIFY ALL TRACKS ADDED SUCCESSFULLY
        // The bulk operation should be atomic - all tracks added or none
        given()
            .when()
                .get("/playlists/{playlistId}/tracks", playlistId)
            .then()
                .statusCode(200)
                .body("size()", equalTo(3))  // All three tracks should be present
                .body("find { it.id == '" + track1Id + "' }.title", equalTo("Hotel California"))
                .body("find { it.id == '" + track2Id + "' }.title", equalTo("Stairway to Heaven"))
                .body("find { it.id == '" + track3Id + "' }.title", equalTo("Sweet Child O' Mine"));

        // Clean up: demonstrate composition relationship again
        given()
            .when()
                .delete("/playlists/{id}", playlistId)
            .then()
                .statusCode(204);

        // All tracks should be deleted due to composition relationship
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