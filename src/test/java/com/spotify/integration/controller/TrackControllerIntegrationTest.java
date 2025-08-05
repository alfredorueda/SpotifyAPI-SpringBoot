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
}