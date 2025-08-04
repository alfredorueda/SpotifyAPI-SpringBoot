package com.spotify.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.controllers.TrackController;
import com.spotify.domain.entities.Track;
import com.spotify.domain.exceptions.TrackNotFoundException;
import com.spotify.dto.CreateTrackRequest;
import com.spotify.dto.UpdateTrackRequest;
import com.spotify.services.TrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TrackController using MockMvc standalone setup.
 * These tests focus on web layer concerns: HTTP handling, JSON serialization,
 * validation, and routing without loading the full Spring context.
 * 
 * @Tag("unit") allows running only unit tests with: mvn test -Dtest="*Test" -Dgroups="unit"
 */
@Tag("unit")
@WebMvcTest(controllers = TrackController.class, includeFilters = @ComponentScan.Filter(ControllerAdvice.class))
@DisplayName("TrackController Unit Tests")
class TrackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrackService trackService;

    @Autowired
    private ObjectMapper objectMapper;

    private Track sampleTrack;
    private CreateTrackRequest validCreateRequest;
    private UpdateTrackRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        sampleTrack = new Track("Bohemian Rhapsody", "Queen", 355);
        validCreateRequest = new CreateTrackRequest("Stairway to Heaven", "Led Zeppelin", 482);
        validUpdateRequest = new UpdateTrackRequest("Hotel California", "Eagles", 391);
    }

    @Nested
    @DisplayName("GET /tracks")
    class GetAllTracks {

        @Test
        @DisplayName("Should return all tracks with 200 OK")
        void shouldReturnAllTracksWithOk() throws Exception {
            // Given
            Track track1 = new Track("Song 1", "Artist 1", 200);
            Track track2 = new Track("Song 2", "Artist 2", 250);
            List<Track> tracks = Arrays.asList(track1, track2);
            
            when(trackService.getAllTracks()).thenReturn(tracks);

            // When & Then
            mockMvc.perform(get("/tracks")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].title", is("Song 1")))
                    .andExpect(jsonPath("$[0].artist", is("Artist 1")))
                    .andExpect(jsonPath("$[0].duration", is(200)))
                    .andExpect(jsonPath("$[1].title", is("Song 2")))
                    .andExpect(jsonPath("$[1].artist", is("Artist 2")))
                    .andExpect(jsonPath("$[1].duration", is(250)));

            verify(trackService, times(1)).getAllTracks();
        }

        @Test
        @DisplayName("Should return empty array when no tracks exist")
        void shouldReturnEmptyArrayWhenNoTracksExist() throws Exception {
            // Given
            when(trackService.getAllTracks()).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/tracks"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(trackService, times(1)).getAllTracks();
        }
    }

    @Nested
    @DisplayName("GET /tracks/{id}")
    class GetTrackById {

        @Test
        @DisplayName("Should return track with 200 OK when track exists")
        void shouldReturnTrackWithOkWhenTrackExists() throws Exception {
            // Given
            String trackId = "test-track-id";
            when(trackService.getTrackById(trackId)).thenReturn(sampleTrack);

            // When & Then
            mockMvc.perform(get("/tracks/{id}", trackId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(sampleTrack.getId())))
                    .andExpect(jsonPath("$.title", is("Bohemian Rhapsody")))
                    .andExpect(jsonPath("$.artist", is("Queen")))
                    .andExpect(jsonPath("$.duration", is(355)));

            verify(trackService, times(1)).getTrackById(trackId);
        }

        @Test
        @DisplayName("Should return 404 when track not found")
        void shouldReturn404WhenTrackNotFound() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            when(trackService.getTrackById(nonExistentId))
                .thenThrow(new TrackNotFoundException(nonExistentId));

            // When & Then
            mockMvc.perform(get("/tracks/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(trackService, times(1)).getTrackById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("POST /tracks")
    class CreateTrack {

        @Test
        @DisplayName("Should create track with 201 CREATED for valid request")
        void shouldCreateTrackWithCreatedForValidRequest() throws Exception {
            // Given
            when(trackService.createTrack(
                validCreateRequest.title(), 
                validCreateRequest.artist(), 
                validCreateRequest.duration()
            )).thenReturn(sampleTrack);

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title", is(sampleTrack.getTitle())))
                    .andExpect(jsonPath("$.artist", is(sampleTrack.getArtist())))
                    .andExpect(jsonPath("$.duration", is(sampleTrack.getDuration())));

            verify(trackService, times(1)).createTrack(
                validCreateRequest.title(), 
                validCreateRequest.artist(), 
                validCreateRequest.duration()
            );
        }

        @Test
        @DisplayName("Should return 400 for request with blank title")
        void shouldReturn400ForRequestWithBlankTitle() throws Exception {
            // Given
            CreateTrackRequest invalidRequest = new CreateTrackRequest("", "Artist", 200);

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }

        @Test
        @DisplayName("Should return 400 for request with blank artist")
        void shouldReturn400ForRequestWithBlankArtist() throws Exception {
            // Given
            CreateTrackRequest invalidRequest = new CreateTrackRequest("Title", "", 200);

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }

        @Test
        @DisplayName("Should return 400 for request with negative duration")
        void shouldReturn400ForRequestWithNegativeDuration() throws Exception {
            // Given
            CreateTrackRequest invalidRequest = new CreateTrackRequest("Title", "Artist", -100);

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }

        @Test
        @DisplayName("Should return 400 for request with null duration")
        void shouldReturn400ForRequestWithNullDuration() throws Exception {
            // Given
            CreateTrackRequest invalidRequest = new CreateTrackRequest("Title", "Artist", null);

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }

        @Test
        @DisplayName("Should return 400 for request with title too long")
        void shouldReturn400ForRequestWithTitleTooLong() throws Exception {
            // Given - Create title longer than 100 characters
            String longTitle = "A".repeat(101);
            CreateTrackRequest invalidRequest = new CreateTrackRequest(longTitle, "Artist", 200);

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }

        @Test
        @DisplayName("Should return 400 for malformed JSON")
        void shouldReturn400ForMalformedJson() throws Exception {
            // Given
            String malformedJson = "{ invalid json }";

            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(malformedJson))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("PUT /tracks/{id}")
    class UpdateTrack {

        @Test
        @DisplayName("Should update track with 200 OK for valid request")
        void shouldUpdateTrackWithOkForValidRequest() throws Exception {
            // Given
            String trackId = "existing-track-id";
            Track updatedTrack = new Track(
                validUpdateRequest.title(), 
                validUpdateRequest.artist(), 
                validUpdateRequest.duration()
            );
            
            when(trackService.updateTrack(
                eq(trackId),
                eq(validUpdateRequest.title()), 
                eq(validUpdateRequest.artist()), 
                eq(validUpdateRequest.duration())
            )).thenReturn(updatedTrack);

            // When & Then
            mockMvc.perform(put("/tracks/{id}", trackId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title", is(validUpdateRequest.title())))
                    .andExpect(jsonPath("$.artist", is(validUpdateRequest.artist())))
                    .andExpect(jsonPath("$.duration", is(validUpdateRequest.duration())));

            verify(trackService, times(1)).updateTrack(
                trackId,
                validUpdateRequest.title(), 
                validUpdateRequest.artist(), 
                validUpdateRequest.duration()
            );
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent track")
        void shouldReturn404WhenUpdatingNonExistentTrack() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            when(trackService.updateTrack(
                eq(nonExistentId), any(), any(), any()
            )).thenThrow(new TrackNotFoundException(nonExistentId));

            // When & Then
            mockMvc.perform(put("/tracks/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isNotFound());

            verify(trackService, times(1)).updateTrack(
                eq(nonExistentId), any(), any(), any()
            );
        }

        @Test
        @DisplayName("Should return 400 for update request with invalid data")
        void shouldReturn400ForUpdateRequestWithInvalidData() throws Exception {
            // Given
            String trackId = "existing-track-id";
            UpdateTrackRequest invalidRequest = new UpdateTrackRequest("", "Artist", 200);

            // When & Then
            mockMvc.perform(put("/tracks/{id}", trackId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).updateTrack(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /tracks/{id}")
    class DeleteTrack {

        @Test
        @DisplayName("Should delete track with 204 NO CONTENT")
        void shouldDeleteTrackWithNoContent() throws Exception {
            // Given
            String trackId = "existing-track-id";
            doNothing().when(trackService).deleteTrack(trackId);

            // When & Then
            mockMvc.perform(delete("/tracks/{id}", trackId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(trackService, times(1)).deleteTrack(trackId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent track")
        void shouldReturn404WhenDeletingNonExistentTrack() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            doThrow(new TrackNotFoundException(nonExistentId))
                .when(trackService).deleteTrack(nonExistentId);

            // When & Then
            mockMvc.perform(delete("/tracks/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(trackService, times(1)).deleteTrack(nonExistentId);
        }
    }

    @Nested
    @DisplayName("HTTP Methods and Content Types")
    class HttpMethodsAndContentTypes {

        @Test
        @DisplayName("Should reject wrong HTTP method for create endpoint")
        void shouldRejectWrongHttpMethodForCreateEndpoint() throws Exception {
            // When & Then
            mockMvc.perform(get("/tracks")
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isOk()); // GET /tracks is valid

            // But PUT to /tracks should not be allowed for creation
            mockMvc.perform(put("/tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("Should require JSON content type for POST requests")
        void shouldRequireJsonContentTypeForPostRequests() throws Exception {
            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should handle missing request body gracefully")
        void shouldHandleMissingRequestBodyGracefully() throws Exception {
            // When & Then
            mockMvc.perform(post("/tracks")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(trackService, never()).createTrack(any(), any(), any());
        }
    }
}