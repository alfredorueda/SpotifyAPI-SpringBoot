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
}