package com.spotify.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.controllers.PlaylistController;
import com.spotify.domain.entities.Playlist;
import com.spotify.domain.entities.Track;
import com.spotify.domain.exceptions.InvalidTrackPositionException;
import com.spotify.domain.exceptions.PlaylistNotFoundException;
import com.spotify.domain.exceptions.TrackNotFoundException;
import com.spotify.dto.*;
import com.spotify.services.PlaylistService;
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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PlaylistController using MockMvc standalone setup.
 * These tests focus on web layer concerns for playlist management:
 * HTTP handling, JSON serialization, validation, and complex routing.
 * 
 * @Tag("unit") allows running only unit tests with: mvn test -Dtest="*Test" -Dgroups="unit"
 */
@Tag("unit")
@WebMvcTest(controllers = PlaylistController.class, includeFilters = @ComponentScan.Filter(ControllerAdvice.class))
@DisplayName("PlaylistController Unit Tests")
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaylistService playlistService;

    @Autowired
    private ObjectMapper objectMapper;

    private Playlist samplePlaylist;
    private Track sampleTrack1;
    private Track sampleTrack2;
    private CreatePlaylistRequest validCreateRequest;
    private UpdatePlaylistRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        samplePlaylist = new Playlist("My Awesome Playlist", true);
        sampleTrack1 = new Track("Bohemian Rhapsody", "Queen", 355);
        sampleTrack2 = new Track("Stairway to Heaven", "Led Zeppelin", 482);
        validCreateRequest = new CreatePlaylistRequest("Test Playlist", false);
        validUpdateRequest = new UpdatePlaylistRequest("Updated Playlist", true);
    }

    @Nested
    @DisplayName("GET /playlists")
    class GetAllPlaylists {

        @Test
        @DisplayName("Should return all playlists with 200 OK")
        void shouldReturnAllPlaylistsWithOk() throws Exception {
            // Given
            Playlist playlist1 = new Playlist("Playlist 1", true);
            Playlist playlist2 = new Playlist("Playlist 2", false);
            List<Playlist> playlists = Arrays.asList(playlist1, playlist2);
            
            when(playlistService.getAllPlaylists()).thenReturn(playlists);

            // When & Then
            mockMvc.perform(get("/playlists"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("Playlist 1")))
                    .andExpect(jsonPath("$[0].isPublic", is(true)))
                    .andExpect(jsonPath("$[1].name", is("Playlist 2")))
                    .andExpect(jsonPath("$[1].isPublic", is(false)));

            verify(playlistService, times(1)).getAllPlaylists();
        }

        @Test
        @DisplayName("Should return empty array when no playlists exist")
        void shouldReturnEmptyArrayWhenNoPlaylistsExist() throws Exception {
            // Given
            when(playlistService.getAllPlaylists()).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/playlists"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(playlistService, times(1)).getAllPlaylists();
        }
    }

    @Nested
    @DisplayName("GET /playlists/{id}")
    class GetPlaylistById {

        @Test
        @DisplayName("Should return playlist with 200 OK when playlist exists")
        void shouldReturnPlaylistWithOkWhenPlaylistExists() throws Exception {
            // Given
            String playlistId = "test-playlist-id";
            when(playlistService.getPlaylistById(playlistId)).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(get("/playlists/{id}", playlistId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is("My Awesome Playlist")))
                    .andExpect(jsonPath("$.isPublic", is(true)));

            verify(playlistService, times(1)).getPlaylistById(playlistId);
        }

        @Test
        @DisplayName("Should return 404 when playlist not found")
        void shouldReturn404WhenPlaylistNotFound() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            when(playlistService.getPlaylistById(nonExistentId))
                .thenThrow(new PlaylistNotFoundException(nonExistentId));

            // When & Then
            mockMvc.perform(get("/playlists/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(playlistService, times(1)).getPlaylistById(nonExistentId);
        }
    }

    @Nested
    @DisplayName("POST /playlists")
    class CreatePlaylist {

        @Test
        @DisplayName("Should create playlist with 201 CREATED for valid request")
        void shouldCreatePlaylistWithCreatedForValidRequest() throws Exception {
            // Given
            when(playlistService.createPlaylist(
                validCreateRequest.name(), 
                validCreateRequest.isPublic()
            )).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(post("/playlists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is(samplePlaylist.getName())))
                    .andExpect(jsonPath("$.isPublic", is(samplePlaylist.getIsPublic())));

            verify(playlistService, times(1)).createPlaylist(
                validCreateRequest.name(), 
                validCreateRequest.isPublic()
            );
        }

        @Test
        @DisplayName("Should return 400 for request with blank name")
        void shouldReturn400ForRequestWithBlankName() throws Exception {
            // Given
            CreatePlaylistRequest invalidRequest = new CreatePlaylistRequest("", true);

            // When & Then
            mockMvc.perform(post("/playlists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }

        @Test
        @DisplayName("Should return 400 for request with null isPublic")
        void shouldReturn400ForRequestWithNullIsPublic() throws Exception {
            // Given
            CreatePlaylistRequest invalidRequest = new CreatePlaylistRequest("Valid Name", null);

            // When & Then
            mockMvc.perform(post("/playlists")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }
    }

    @Nested
    @DisplayName("PUT /playlists/{id}")
    class UpdatePlaylist {

        @Test
        @DisplayName("Should update playlist with 200 OK for valid request")
        void shouldUpdatePlaylistWithOkForValidRequest() throws Exception {
            // Given
            String playlistId = "existing-playlist-id";
            when(playlistService.updatePlaylist(
                eq(playlistId),
                eq(validUpdateRequest.name()), 
                eq(validUpdateRequest.isPublic())
            )).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(put("/playlists/{id}", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is(samplePlaylist.getName())))
                    .andExpect(jsonPath("$.isPublic", is(samplePlaylist.getIsPublic())));

            verify(playlistService, times(1)).updatePlaylist(
                playlistId,
                validUpdateRequest.name(), 
                validUpdateRequest.isPublic()
            );
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent playlist")
        void shouldReturn404WhenUpdatingNonExistentPlaylist() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            when(playlistService.updatePlaylist(eq(nonExistentId), any(), any()))
                .thenThrow(new PlaylistNotFoundException(nonExistentId));

            // When & Then
            mockMvc.perform(put("/playlists/{id}", nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isNotFound());

            verify(playlistService, times(1)).updatePlaylist(eq(nonExistentId), any(), any());
        }
    }

    @Nested
    @DisplayName("DELETE /playlists/{id}")
    class DeletePlaylist {

        @Test
        @DisplayName("Should delete playlist with 204 NO CONTENT")
        void shouldDeletePlaylistWithNoContent() throws Exception {
            // Given
            String playlistId = "existing-playlist-id";
            doNothing().when(playlistService).deletePlaylist(playlistId);

            // When & Then
            mockMvc.perform(delete("/playlists/{id}", playlistId))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(playlistService, times(1)).deletePlaylist(playlistId);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent playlist")
        void shouldReturn404WhenDeletingNonExistentPlaylist() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            doThrow(new PlaylistNotFoundException(nonExistentId))
                .when(playlistService).deletePlaylist(nonExistentId);

            // When & Then
            mockMvc.perform(delete("/playlists/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

            verify(playlistService, times(1)).deletePlaylist(nonExistentId);
        }
    }

    @Nested
    @DisplayName("GET /playlists/{playlistId}/tracks")
    class GetPlaylistTracks {

        @Test
        @DisplayName("Should return playlist tracks with 200 OK")
        void shouldReturnPlaylistTracksWithOk() throws Exception {
            // Given
            String playlistId = "playlist-id";
            List<Track> tracks = Arrays.asList(sampleTrack1, sampleTrack2);
            
            when(playlistService.getPlaylistTracks(playlistId)).thenReturn(tracks);

            // When & Then
            mockMvc.perform(get("/playlists/{playlistId}/tracks", playlistId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].title", is("Bohemian Rhapsody")))
                    .andExpect(jsonPath("$[0].artist", is("Queen")))
                    .andExpect(jsonPath("$[1].title", is("Stairway to Heaven")))
                    .andExpect(jsonPath("$[1].artist", is("Led Zeppelin")));

            verify(playlistService, times(1)).getPlaylistTracks(playlistId);
        }

        @Test
        @DisplayName("Should return empty array for playlist with no tracks")
        void shouldReturnEmptyArrayForPlaylistWithNoTracks() throws Exception {
            // Given
            String playlistId = "empty-playlist-id";
            when(playlistService.getPlaylistTracks(playlistId)).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/playlists/{playlistId}/tracks", playlistId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(playlistService, times(1)).getPlaylistTracks(playlistId);
        }
    }

    @Nested
    @DisplayName("POST /playlists/{playlistId}/tracks")
    class AddTrackToPlaylist {

        @Test
        @DisplayName("Should add track to playlist with 200 OK")
        void shouldAddTrackToPlaylistWithOk() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddTrackToPlaylistRequest request = new AddTrackToPlaylistRequest("track-id");
            
            when(playlistService.addTrackToPlaylist(playlistId, "track-id")).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is(samplePlaylist.getName())));

            verify(playlistService, times(1)).addTrackToPlaylist(playlistId, "track-id");
        }

        @Test
        @DisplayName("Should return 400 for request with blank trackId")
        void shouldReturn400ForRequestWithBlankTrackId() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddTrackToPlaylistRequest invalidRequest = new AddTrackToPlaylistRequest("");

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }

        @Test
        @DisplayName("Should return 404 when track not found")
        void shouldReturn404WhenTrackNotFound() throws Exception {
            // Given
            String playlistId = "playlist-id";
            String nonExistentTrackId = "non-existent-track-id";
            AddTrackToPlaylistRequest request = new AddTrackToPlaylistRequest(nonExistentTrackId);
            
            when(playlistService.addTrackToPlaylist(playlistId, nonExistentTrackId))
                .thenThrow(new TrackNotFoundException(nonExistentTrackId));

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(playlistService, times(1)).addTrackToPlaylist(playlistId, nonExistentTrackId);
        }
    }

    @Nested
    @DisplayName("POST /playlists/{playlistId}/tracks/position")
    class AddTrackAtPosition {

        @Test
        @DisplayName("Should add track at position with 200 OK")
        void shouldAddTrackAtPositionWithOk() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddTrackAtPositionRequest request = new AddTrackAtPositionRequest("track-id", 0);
            
            when(playlistService.addTrackAtPosition(playlistId, "track-id", 0)).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/position", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is(samplePlaylist.getName())));

            verify(playlistService, times(1)).addTrackAtPosition(playlistId, "track-id", 0);
        }

        @Test
        @DisplayName("Should return 400 for negative position")
        void shouldReturn400ForNegativePosition() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddTrackAtPositionRequest invalidRequest = new AddTrackAtPositionRequest("track-id", -1);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/position", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }

        @Test
        @DisplayName("Should return 400 when position exceeds playlist bounds")
        void shouldReturn400WhenPositionExceedsPlaylistBounds() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddTrackAtPositionRequest request = new AddTrackAtPositionRequest("track-id", 100);
            
            when(playlistService.addTrackAtPosition(playlistId, "track-id", 100))
                .thenThrow(new InvalidTrackPositionException(100, 0));

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/position", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(playlistService, times(1)).addTrackAtPosition(playlistId, "track-id", 100);
        }
    }

    @Nested
    @DisplayName("POST /playlists/{playlistId}/tracks/multiple")
    class AddMultipleTracks {

        @Test
        @DisplayName("Should add multiple tracks with 200 OK")
        void shouldAddMultipleTracksWithOk() throws Exception {
            // Given
            String playlistId = "playlist-id";
            List<String> trackIds = Arrays.asList("track1-id", "track2-id");
            AddMultipleTracksRequest request = new AddMultipleTracksRequest(trackIds, null);
            
            when(playlistService.addMultipleTracks(playlistId, request)).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/multiple", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name", is(samplePlaylist.getName())));

            verify(playlistService, times(1)).addMultipleTracks(playlistId, request);
        }

        @Test
        @DisplayName("Should add multiple tracks at specific position")
        void shouldAddMultipleTracksAtSpecificPosition() throws Exception {
            // Given
            String playlistId = "playlist-id";
            List<String> trackIds = Arrays.asList("track1-id", "track2-id");
            AddMultipleTracksRequest request = new AddMultipleTracksRequest(trackIds, 1);
            
            when(playlistService.addMultipleTracks(playlistId, request)).thenReturn(samplePlaylist);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/multiple", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(samplePlaylist.getName())));

            verify(playlistService, times(1)).addMultipleTracks(playlistId, request);
        }

        @Test
        @DisplayName("Should return 400 for empty trackIds list")
        void shouldReturn400ForEmptyTrackIdsList() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddMultipleTracksRequest invalidRequest = new AddMultipleTracksRequest(Arrays.asList(), 0);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/multiple", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }

        @Test
        @DisplayName("Should return 400 for null trackIds list")
        void shouldReturn400ForNullTrackIdsList() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddMultipleTracksRequest invalidRequest = new AddMultipleTracksRequest(null, 0);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/multiple", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }

        @Test
        @DisplayName("Should return 400 for negative position in multiple tracks")
        void shouldReturn400ForNegativePositionInMultipleTracks() throws Exception {
            // Given
            String playlistId = "playlist-id";
            List<String> trackIds = Arrays.asList("track1-id", "track2-id");
            AddMultipleTracksRequest invalidRequest = new AddMultipleTracksRequest(trackIds, -1);

            // When & Then
            mockMvc.perform(post("/playlists/{playlistId}/tracks/multiple", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(playlistService);
        }
    }

    @Nested
    @DisplayName("Complex Routing and HTTP Semantics")
    class ComplexRoutingAndHttpSemantics {

        @Test
        @DisplayName("Should handle nested resource paths correctly")
        void shouldHandleNestedResourcePathsCorrectly() throws Exception {
            // Given
            String playlistId = "playlist-123";
            List<Track> tracks = Arrays.asList(sampleTrack1);
            when(playlistService.getPlaylistTracks(playlistId)).thenReturn(tracks);

            // When & Then
            mockMvc.perform(get("/playlists/{playlistId}/tracks", playlistId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(playlistService, times(1)).getPlaylistTracks(playlistId);
        }

        @Test
        @DisplayName("Should differentiate between similar endpoints")
        void shouldDifferentiateBetweenSimilarEndpoints() throws Exception {
            // Given
            String playlistId = "playlist-id";
            AddTrackToPlaylistRequest simpleRequest = new AddTrackToPlaylistRequest("track-id");
            AddTrackAtPositionRequest positionRequest = new AddTrackAtPositionRequest("track-id", 0);
            
            when(playlistService.addTrackToPlaylist(playlistId, "track-id")).thenReturn(samplePlaylist);
            when(playlistService.addTrackAtPosition(playlistId, "track-id", 0)).thenReturn(samplePlaylist);

            // When & Then - Test different endpoints
            mockMvc.perform(post("/playlists/{playlistId}/tracks", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(simpleRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/playlists/{playlistId}/tracks/position", playlistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(positionRequest)))
                    .andExpect(status().isOk());

            verify(playlistService, times(1)).addTrackToPlaylist(playlistId, "track-id");
            verify(playlistService, times(1)).addTrackAtPosition(playlistId, "track-id", 0);
        }

        @Test
        @DisplayName("Should reject invalid HTTP methods for specific endpoints")
        void shouldRejectInvalidHttpMethodsForSpecificEndpoints() throws Exception {
            // When & Then
            mockMvc.perform(delete("/playlists/123/tracks"))
                    .andExpect(status().isMethodNotAllowed());

            mockMvc.perform(put("/playlists/123/tracks/position"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}