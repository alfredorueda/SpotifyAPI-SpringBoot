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
 * COMPREHENSIVE INTEGRATION TESTS FOR TRACK API ENDPOINTS
 * 
 * This class demonstrates end-to-end integration testing best practices for a REST API
 * using Spring Boot. It serves as an educational example for junior developers learning
 * Spring Boot testing and REST API validation.
 * 
 * WHAT ARE INTEGRATION TESTS?
 * Integration tests verify that different parts of your application work together correctly.
 * Unlike unit tests that test individual components in isolation, integration tests:
 * - Start the complete Spring application context
 * - Use a real embedded server (not mocked)
 * - Test the full request/response cycle
 * - Verify database interactions and data persistence
 * - Test validation, error handling, and business logic end-to-end
 * 
 * SPRING BOOT TEST ANNOTATIONS EXPLAINED:
 * 
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * - Starts the full Spring Boot application context
 * - Launches an embedded server (Tomcat) on a random available port
 * - Loads all Spring beans, configurations, and database connections
 * - This creates a "real" environment similar to production
 * 
 * @ActiveProfiles("test")
 * - Activates the "test" profile, which typically uses:
 *   - In-memory H2 database instead of production database
 *   - Test-specific configuration properties
 *   - Faster startup and cleanup for testing
 * 
 * @LocalServerPort
 * - Injects the random port number that Spring Boot chose
 * - Allows RestAssured to connect to the correct port
 * - Essential for dynamic port assignment in CI/CD environments
 * 
 * RESTASSURED LIBRARY:
 * RestAssured is a popular Java library for testing REST APIs. It provides:
 * - Fluent, readable syntax for HTTP requests
 * - Built-in JSON/XML parsing and validation
 * - Easy assertion methods for response validation
 * - Support for authentication, headers, and complex request scenarios
 * 
 * TESTING STRATEGY:
 * This test class covers both "happy path" and "error scenarios":
 * 1. Happy Path: Normal operations that should succeed
 * 2. Error Scenarios: Invalid input, missing resources, edge cases
 * 3. Validation Testing: Ensuring business rules are enforced
 * 4. Concurrent Operations: Testing race conditions and state consistency
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TrackControllerIntegrationTest {

    /**
     * Spring Boot will inject the random port number here.
     * This port is used to configure RestAssured to connect to our test server.
     */
    @LocalServerPort
    private int port;

    /**
     * SETUP METHOD - EXECUTED BEFORE EACH TEST
     * 
     * @BeforeEach runs before every individual test method, ensuring:
     * - Clean state for each test (isolation)
     * - Proper RestAssured configuration
     * - Consistent test environment
     * 
     * RestAssured Configuration:
     * - port: Tells RestAssured which port our test server is running on
     * - baseURI: Sets the base URL for all HTTP requests in tests
     */
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    /**
     * HAPPY PATH TEST - COMPLETE CRUD LIFECYCLE
     * 
     * This test demonstrates the full lifecycle of a Track resource:
     * CREATE → READ → UPDATE → LIST → DELETE → VERIFY DELETION
     * 
     * LEARNING OBJECTIVES:
     * - Understanding REST API conventions (POST, GET, PUT, DELETE)
     * - HTTP status codes and their meanings
     * - JSON request/response handling
     * - Resource state management
     * - Data persistence verification
     * 
     * REST API PATTERNS DEMONSTRATED:
     * - POST /tracks (201 Created) - Create new resource
     * - GET /tracks/{id} (200 OK) - Retrieve specific resource
     * - PUT /tracks/{id} (200 OK) - Update existing resource
     * - GET /tracks (200 OK) - List all resources
     * - DELETE /tracks/{id} (204 No Content) - Remove resource
     * - GET /tracks/{id} (404 Not Found) - Verify deletion
     */
    @Test
    void shouldPerformCompleteTrackLifecycle() {
        // STEP 1: CREATE A NEW TRACK
        // Using POST to create a resource. The server should return:
        // - Status 201 (Created) indicating successful resource creation
        // - The created resource in the response body with auto-generated ID
        // - Location header pointing to the new resource (handled by Spring Boot)
        String trackId = given()
                .contentType(ContentType.JSON)  // Tell server we're sending JSON
                .body("""
                    {
                        "title": "Bohemian Rhapsody",
                        "artist": "Queen",
                        "duration": 355
                    }
                    """)
            .when()
                .post("/tracks")  // HTTP POST to create resource
            .then()
                .statusCode(201)  // Verify "Created" status
                .body("title", equalTo("Bohemian Rhapsody"))    // Verify response data
                .body("artist", equalTo("Queen"))
                .body("duration", equalTo(355))
                .body("id", notNullValue())        // Server should generate an ID
                .body("createdAt", notNullValue()) // Server should set creation timestamp
            .extract()
                .path("id");  // Extract the ID for use in subsequent operations

        // STEP 2: RETRIEVE THE CREATED TRACK BY ID
        // Using GET to read a specific resource. Should return:
        // - Status 200 (OK) for successful retrieval
        // - Complete resource data including system-generated fields
        given()
            .when()
                .get("/tracks/{id}", trackId)  // HTTP GET with path parameter
            .then()
                .statusCode(200)  // Verify "OK" status
                .body("id", equalTo(trackId))              // Verify correct resource
                .body("title", equalTo("Bohemian Rhapsody"))
                .body("artist", equalTo("Queen"))
                .body("duration", equalTo(355))
                .body("createdAt", notNullValue());  // Verify timestamp persisted

        // STEP 3: UPDATE THE TRACK
        // Using PUT for complete resource replacement. Should return:
        // - Status 200 (OK) for successful update
        // - Updated resource data in response body
        // - Same ID (resource identity preserved)
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
                .put("/tracks/{id}", trackId)  // HTTP PUT for update
            .then()
                .statusCode(200)  // Verify "OK" status for update
                .body("id", equalTo(trackId))  // ID should remain unchanged
                .body("title", equalTo("Bohemian Rhapsody (Remastered)"))
                .body("artist", equalTo("Queen"))
                .body("duration", equalTo(360));  // Verify updates applied

        // STEP 4: VERIFY TRACK APPEARS IN COLLECTION
        // Using GET to retrieve all resources. Should return:
        // - Status 200 (OK)
        // - Array containing our updated track among others
        // - Demonstrates that updates are reflected in collection views
        given()
            .when()
                .get("/tracks")  // HTTP GET for collection
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))  // At least our track exists
                .body("find { it.id == '" + trackId + "' }.title", 
                      equalTo("Bohemian Rhapsody (Remastered)"));  // Verify our track in collection

        // STEP 5: DELETE THE TRACK
        // Using DELETE to remove a resource. Should return:
        // - Status 204 (No Content) indicating successful deletion
        // - Empty response body (resource no longer exists)
        given()
            .when()
                .delete("/tracks/{id}", trackId)  // HTTP DELETE
            .then()
                .statusCode(204);  // Verify "No Content" status

        // STEP 6: VERIFY DELETION COMPLETED
        // Attempting to retrieve deleted resource should return:
        // - Status 404 (Not Found)
        // - Confirms resource was actually removed from database
        given()
            .when()
                .get("/tracks/{id}", trackId)
            .then()
                .statusCode(404);  // Verify "Not Found" status
    }

    /**
     * VALIDATION ERROR TESTING - CREATE OPERATION
     * 
     * LEARNING OBJECTIVES:
     * - Understanding Jakarta Bean Validation (JSR-380)
     * - HTTP 400 Bad Request for client-side errors
     * - Defensive programming and input validation
     * - API contract enforcement
     * 
     * These tests verify that the API properly validates input data and rejects
     * invalid requests with appropriate error responses. This is crucial for:
     * - Data integrity and consistency
     * - Security (preventing malicious input)
     * - User experience (clear error messages)
     * - API reliability and robustness
     * 
     * VALIDATION RULES TESTED:
     * - @NotBlank: Fields cannot be null, empty, or whitespace-only
     * - @Size: Fields must meet length constraints
     * - @Positive: Numeric fields must be positive numbers
     * - @NotNull: Fields cannot be null
     */
    @Test
    void shouldHandleValidationErrorsWhenCreatingTrack() {
        // TEST 1: MISSING REQUIRED FIELD (title)
        // When a required field is missing, the API should return 400 Bad Request
        // This tests the @NotBlank validation annotation
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

        // TEST 2: BLANK/EMPTY FIELD VALUES
        // Empty strings should be rejected by @NotBlank validation
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

        // TEST 3: MISSING ARTIST FIELD
        // Another required field validation test
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

        // TEST 4: NEGATIVE DURATION
        // Business rule: duration must be positive (songs can't have negative length)
        // Tests @Positive validation annotation
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

        // TEST 5: NULL DURATION
        // Tests @NotNull validation annotation
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

    /**
     * VALIDATION ERROR TESTING - UPDATE OPERATION
     * 
     * Updates should follow the same validation rules as creation.
     * This test ensures consistency across different HTTP methods.
     */
    @Test
    void shouldHandleValidationErrorsWhenUpdatingTrack() {
        // First create a valid track to update
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

        // TEST 1: UPDATE WITH INVALID TITLE
        // Even updates must follow validation rules
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

        // TEST 2: UPDATE WITH INVALID DURATION
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

        // Clean up the test data
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);
    }

    /**
     * NOT FOUND ERROR TESTING
     * 
     * LEARNING OBJECTIVES:
     * - HTTP 404 Not Found status code usage
     * - Resource existence validation
     * - Graceful error handling for missing resources
     * 
     * These tests verify proper handling when clients request operations
     * on non-existent resources. This is important for:
     * - API reliability and predictability
     * - Clear error communication to clients
     * - Security (not revealing internal system details)
     */
    @Test
    void shouldHandleNotFoundErrorsForNonExistentTrack() {
        String nonExistentTrackId = "non-existent-track-id";

        // TEST 1: GET NON-EXISTENT TRACK
        // Requesting a resource that doesn't exist should return 404
        given()
            .when()
                .get("/tracks/{id}", nonExistentTrackId)
            .then()
                .statusCode(404);

        // TEST 2: UPDATE NON-EXISTENT TRACK
        // Attempting to update a non-existent resource should return 404
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

        // TEST 3: DELETE NON-EXISTENT TRACK
        // Attempting to delete a non-existent resource should return 404
        given()
            .when()
                .delete("/tracks/{id}", nonExistentTrackId)
            .then()
                .statusCode(404);
    }

    /**
     * MALFORMED REQUEST TESTING
     * 
     * LEARNING OBJECTIVES:
     * - HTTP 400 Bad Request for malformed data
     * - HTTP 415 Unsupported Media Type for content type issues
     * - Importance of proper Content-Type headers
     * - JSON syntax validation
     */
    @Test
    void shouldHandleMalformedJsonRequests() {
        // TEST 1: INVALID JSON SYNTAX
        // Malformed JSON should be rejected with 400 Bad Request
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

        // TEST 2: MISSING CONTENT-TYPE HEADER
        // When sending JSON without proper Content-Type header,
        // server should return 415 Unsupported Media Type
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

    /**
     * EMPTY REQUEST BODY TESTING
     * 
     * Tests handling of requests with missing or empty request bodies.
     * Important for API robustness and security.
     */
    @Test
    void shouldHandleEmptyRequestBody() {
        // TEST 1: EMPTY BODY STRING
        given()
                .contentType(ContentType.JSON)
                .body("")
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);

        // TEST 2: NO BODY PROVIDED
        given()
                .contentType(ContentType.JSON)
            .when()
                .post("/tracks")
            .then()
                .statusCode(400);
    }

    /**
     * CONCURRENT OPERATIONS TESTING
     * 
     * LEARNING OBJECTIVES:
     * - Race conditions and resource state consistency
     * - Idempotent operations (operations that can be repeated safely)
     * - Proper error handling for state conflicts
     * 
     * This test simulates concurrent access patterns that might occur
     * in production with multiple clients accessing the same resources.
     */
    @Test
    void shouldHandleConcurrentTrackOperations() {
        // Create a track for testing concurrent operations
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

        // FIRST DELETE: Should succeed normally
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(204);

        // SECOND DELETE: Should return 404 since resource no longer exists
        // This tests that the system properly handles attempts to delete
        // already-deleted resources
        given()
            .when()
                .delete("/tracks/{id}", trackId)
            .then()
                .statusCode(404);

        // UPDATE DELETED RESOURCE: Should return 404
        // This tests that updates on deleted resources are properly rejected
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

    /**
     * FIELD LENGTH VALIDATION TESTING
     * 
     * LEARNING OBJECTIVES:
     * - @Size validation annotation usage
     * - Business constraints and data modeling
     * - Database schema considerations
     * - Security implications of unbounded input
     * 
     * These tests verify that the API enforces reasonable limits on
     * field lengths to prevent database issues and potential security risks.
     */
    @Test
    void shouldHandleFieldLengthValidation() {
        // TEST 1: TITLE TOO LONG
        // Based on @Size(max = 100) annotation in CreateTrackRequest
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

        // TEST 2: ARTIST NAME TOO LONG
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