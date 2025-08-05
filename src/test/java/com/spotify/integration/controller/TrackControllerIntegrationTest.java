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
 * End-to-end integration tests for Track API endpoints.
 * Tests the complete flow from REST controller through service, repository, and database.
 * 
 * Uses @SpringBootTest with webEnvironment.RANDOM_PORT to start the full Spring context
 * with an embedded server for true end-to-end testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TrackControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void shouldPerformCompleteTrackLifecycle() {
        // 1. Create a new track (POST /tracks)
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
                .body("title", equalTo("Bohemian Rhapsody"))
                .body("artist", equalTo("Queen"))
                .body("duration", equalTo(355))
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
            .extract()
                .path("id");

        // 2. Retrieve the created track by ID (GET /tracks/{id})
        given()
            .when()
                .get("/tracks/{id}", trackId)
            .then()
                .statusCode(200)
                .body("id", equalTo(trackId))
                .body("title", equalTo("Bohemian Rhapsody"))
                .body("artist", equalTo("Queen"))
                .body("duration", equalTo(355))
                .body("createdAt", notNullValue());

        // 3. Update the track (PUT /tracks/{id})
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Bohemian Rhapsody (Remastered)",
                        "artist": "Queen",
                        "duration": 360
                    }
                    """)
            .when()
                .put("/tracks/{id}", trackId)
            .then()
                .statusCode(200)
                .body("id", equalTo(trackId))
                .body("title", equalTo("Bohemian Rhapsody (Remastered)"))
                .body("artist", equalTo("Queen"))
                .body("duration", equalTo(360));

        // 4. Verify the track appears in the list of all tracks (GET /tracks)
        given()
            .when()
                .get("/tracks")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.id == '" + trackId + "' }.title", 
                      equalTo("Bohemian Rhapsody (Remastered)"));

        // 5. Delete the track (DELETE /tracks/{id})
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);

        // 6. Verify the track is no longer accessible (GET /tracks/{id})
        given()
            .when()
                .get("/tracks/{id}", trackId)
            .then()
                .statusCode(404);
    }

    @Test
    void shouldHandleValidationErrorsWhenCreatingTrack() {
        // Test 1: Missing title
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "artist": "Queen",
                        "duration": 355
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400); // Bad Request for validation error

        // Test 2: Blank title
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "",
                        "artist": "Queen",
                        "duration": 355
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // Test 3: Missing artist
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Bohemian Rhapsody",
                        "duration": 355
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // Test 4: Negative duration
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Bohemian Rhapsody",
                        "artist": "Queen",
                        "duration": -100
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // Test 5: Null duration
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Bohemian Rhapsody",
                        "artist": "Queen",
                        "duration": null
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);
    }

    @Test
    void shouldHandleValidationErrorsWhenUpdatingTrack() {
        // First create a valid track
        String trackId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Test Track",
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

        // Test 1: Try to update with blank title
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "",
                        "artist": "Updated Artist",
                        "duration": 200
                    }
                    """)
            .when()
                .put("/tracks/{id}", trackId)
            .then()
                .statusCode(400);

        // Test 2: Try to update with negative duration
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Updated Track",
                        "artist": "Updated Artist",
                        "duration": -50
                    }
                    """)
            .when()
                .put("/tracks/{id}", trackId)
            .then()
                .statusCode(400);

        // Clean up
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);
    }

    @Test
    void shouldHandleNotFoundErrorsForNonExistentTrack() {
        String nonExistentTrackId = "non-existent-track-id";

        // Test 1: GET non-existent track
        given()
            .when()
                .get("/tracks/{id}", nonExistentTrackId)
            .then()
                .statusCode(404);

        // Test 2: PUT (update) non-existent track
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Updated Title",
                        "artist": "Updated Artist",
                        "duration": 200
                    }
                    """)
            .when()
                .put("/tracks/{id}", nonExistentTrackId)
            .then()
                .statusCode(404);

        // Test 3: DELETE non-existent track
        given()
            .when()
                .delete("/tracks/{id}", nonExistentTrackId)
            .then()
                .statusCode(404);
    }

    @Test
    void shouldHandleMalformedJsonRequests() {
        // Test 1: Invalid JSON syntax
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Test Track",
                        "artist": "Test Artist",
                        "duration": 180,
                    }
                    """) // Note the trailing comma which makes it invalid JSON
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // Test 2: Missing content type
        given()
                .body("""
                    {
                        "title": "Test Track",
                        "artist": "Test Artist",
                        "duration": 180
                    }
                    """)
            .when()
                .post("/tracks")
            .then()
                .statusCode(415); // Unsupported Media Type
    }

    @Test
    void shouldHandleEmptyRequestBody() {
        // Test 1: Empty body for POST
        given()
                .contentType(ContentType.JSON)
                .body("")
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // Test 2: Null body for POST
        given()
                .contentType(ContentType.JSON)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);
    }

    @Test
    void shouldHandleConcurrentTrackOperations() {
        // Create a track
        String trackId = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Concurrent Test Track",
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

        // First delete should succeed
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);

        // Second delete of same track should return 404
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(404);

        // Try to update deleted track should return 404
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Updated Title",
                        "artist": "Updated Artist", 
                        "duration": 200
                    }
                    """)
            .when()
                .put("/tracks/{id}", trackId)
            .then()
                .statusCode(404);
    }

    @Test
    void shouldHandleFieldLengthValidation() {
        // Test with title too long (assuming max 100 characters based on CreateTrackRequest)
        String longTitle = "A".repeat(101);
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "%s",
                        "artist": "Test Artist",
                        "duration": 180
                    }
                    """.formatted(longTitle))
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // Test with artist too long (assuming max 100 characters)
        String longArtist = "B".repeat(101);
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "title": "Test Track",
                        "artist": "%s",
                        "duration": 180
                    }
                    """.formatted(longArtist))
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);
    }
}