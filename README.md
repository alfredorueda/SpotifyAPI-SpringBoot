# Spotify-like REST API

Spring Boot application implementing a simplified Spotify-like REST API following Domain-Driven Design (DDD) principles with a rich domain model.

## 🏗️ Architecture & Design Principles

This application demonstrates:

- **Domain-Driven Design (DDD)**: Rich domain entities with encapsulated business logic
- **Application-Generated IDs**: UUID-based alphanumeric identifiers for microservices readiness
- **REST API Best Practices**: Proper HTTP methods, status codes, and resource naming
- **Type-Safe DTOs**: Java 21 records for request payloads instead of Map<String, Object>
- **Clean Architecture**: Thin service layer delegating to rich domain entities
- **Constructor-Based Dependency Injection**: Following Spring Boot best practices

## 🚀 Technology Stack

- **Java 21** with modern features (records, pattern matching)
- **Spring Boot 3.2.1** (latest stable)
- **Spring Web** for REST API with type-safe DTOs
- **Spring Data JPA** for persistence
- **MySQL 8.0** for database
- **Maven** for build management

## 📦 Domain Model

### Entities

#### Track
- `id`: Application-generated UUID string
- `title`: Track title
- `artist`: Artist name
- `duration`: Duration in seconds
- `createdAt`: Auto-populated timestamp

#### Playlist (Rich Domain Entity)
- `id`: Application-generated UUID string
- `name`: Playlist name
- `isPublic`: Visibility flag
- `createdAt`: Auto-populated timestamp
- `tracks`: Ordered list of tracks with position management

**Rich Domain Methods:**
- `addTrack(Track track)`: Adds track to end of playlist
- `addTrackAtPosition(Track track, int position)`: Adds track at specific position
- `removeTrack(Track track)`: Removes track from playlist
- `getTotalDuration()`: Calculates total playlist duration
- `getTrackCount()`: Returns number of tracks

### DTOs (Request Models)

**Type-Safe Request DTOs using Java 21 Records:**
- `CreateTrackRequest(String title, String artist, Integer duration)`
- `UpdateTrackRequest(String title, String artist, Integer duration)`
- `CreatePlaylistRequest(String name, Boolean isPublic)`
- `UpdatePlaylistRequest(String name, Boolean isPublic)`
- `AddTrackToPlaylistRequest(String trackId)`
- `AddTrackAtPositionRequest(String trackId, Integer position)`

## 🛠️ Setup Instructions

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Docker and Docker Compose (for MySQL)

### 1. Start MySQL Database

Create a `docker-compose.yml` file in the project root:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: spotify-mysql
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: spotify_api
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

Start the database:
```bash
docker-compose up -d
```

### 2. Build and Run the Application

```bash
# Clone/navigate to project directory
cd spotify-api

# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### 3. Verify Setup

Check if the application is running:
```bash
curl http://localhost:8080/tracks
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Track Endpoints

#### Get All Tracks
```http
GET /tracks
```
**Response: 200 OK**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "title": "Bohemian Rhapsody",
    "artist": "Queen",
    "duration": 355,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

#### Get Track by ID
```http
GET /tracks/{id}
```
**Response: 200 OK** (same as above single object)
**Response: 404 NOT FOUND** if track doesn't exist

#### Create Track (Using Type-Safe DTO)
```http
POST /tracks
Content-Type: application/json

{
  "title": "Bohemian Rhapsody",
  "artist": "Queen",
  "duration": 355
}
```
**Response: 201 CREATED**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Bohemian Rhapsody",
  "artist": "Queen",
  "duration": 355,
  "createdAt": "2024-01-15T10:30:00"
}
```

**DTO Validation:** The request automatically validates:
- `title` and `artist` cannot be null or empty
- `duration` must be a positive number

#### Update Track (Using Type-Safe DTO)
```http
PUT /tracks/{id}
Content-Type: application/json

{
  "title": "Bohemian Rhapsody (Remastered)",
  "artist": "Queen",
  "duration": 355
}
```
**Response: 200 OK** (returns updated track)

#### Delete Track
```http
DELETE /tracks/{id}
```
**Response: 204 NO CONTENT**

### Playlist Endpoints

#### Get All Playlists
```http
GET /playlists
```
**Response: 200 OK**
```json
[
  {
    "id": "p1l2a3y4-l5i6-7890-abcd-ef1234567890",
    "name": "My Favorites",
    "isPublic": true,
    "createdAt": "2024-01-15T10:30:00",
    "tracks": [...]
  }
]
```

#### Get Playlist by ID
```http
GET /playlists/{id}
```
**Response: 200 OK** (same as above single object)
**Response: 404 NOT FOUND** if playlist doesn't exist

#### Create Playlist (Using Type-Safe DTO)
```http
POST /playlists
Content-Type: application/json

{
  "name": "My Favorites",
  "isPublic": true
}
```
**Response: 201 CREATED**
```json
{
  "id": "p1l2a3y4-l5i6-7890-abcd-ef1234567890",
  "name": "My Favorites",
  "isPublic": true,
  "createdAt": "2024-01-15T10:30:00",
  "tracks": []
}
```

**DTO Validation:** 
- `name` cannot be null or empty
- `isPublic` cannot be null

#### Update Playlist (Using Type-Safe DTO)
```http
PUT /playlists/{id}
Content-Type: application/json

{
  "name": "My Updated Favorites",
  "isPublic": false
}
```
**Response: 200 OK** (returns updated playlist)

#### Delete Playlist
```http
DELETE /playlists/{id}
```
**Response: 204 NO CONTENT**

#### Get Playlist Tracks
```http
GET /playlists/{playlistId}/tracks
```
**Response: 200 OK**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "title": "Bohemian Rhapsody",
    "artist": "Queen",
    "duration": 355,
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

#### Add Track to Playlist (Using Type-Safe DTO)
```http
POST /playlists/{playlistId}/tracks
Content-Type: application/json

{
  "trackId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```
**Response: 200 OK** (returns updated playlist)

**DTO Validation:** `trackId` cannot be null or empty

#### Add Track at Specific Position (Using Type-Safe DTO)
```http
POST /playlists/{playlistId}/tracks/position
Content-Type: application/json

{
  "trackId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "position": 0
}
```
**Response: 200 OK** (returns updated playlist)
**Response: 400 BAD REQUEST** if position is invalid

**DTO Validation:**
- `trackId` cannot be null or empty
- `position` must be non-negative

### Error Responses

All error responses follow this structure:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "TRACK_NOT_FOUND",
  "message": "Track not found with ID: invalid-id"
}
```

**Error Types:**
- `TRACK_NOT_FOUND` (404)
- `PLAYLIST_NOT_FOUND` (404)
- `INVALID_TRACK_POSITION` (400)
- `VALIDATION_ERROR` (400) - From DTO validation
- `DOMAIN_ERROR` (400)
- `INTERNAL_ERROR` (500)

## 🧪 Testing Examples

### Create and Manage Tracks
```bash
# Create a track with type-safe payload
curl -X POST http://localhost:8080/tracks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Bohemian Rhapsody",
    "artist": "Queen",
    "duration": 355
  }'

# Get all tracks
curl http://localhost:8080/tracks

# Update a track
curl -X PUT http://localhost:8080/tracks/{track-id} \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Bohemian Rhapsody (Remastered)",
    "artist": "Queen",
    "duration": 355
  }'
```

### Create and Manage Playlists
```bash
# Create a playlist with validated payload
curl -X POST http://localhost:8080/playlists \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rock Classics",
    "isPublic": true
  }'

# Add track to playlist
curl -X POST http://localhost:8080/playlists/{playlist-id}/tracks \
  -H "Content-Type: application/json" \
  -d '{
    "trackId": "{track-id}"
  }'

# Add track at specific position
curl -X POST http://localhost:8080/playlists/{playlist-id}/tracks/position \
  -H "Content-Type: application/json" \
  -d '{
    "trackId": "{track-id}",
    "position": 0
  }'
```

## 🏛️ DDD Architecture Highlights

### Rich Domain Model
- **Playlist** entity encapsulates business logic for track management
- Domain methods enforce invariants (position validation, duplicate prevention)
- Application-generated UUIDs prepare for microservices architecture

### Type-Safe API Layer
- **Java 21 Records**: Immutable DTOs with built-in validation
- **Compile-time Safety**: No more `ClassCastException` from map casting
- **Self-Documenting**: DTOs serve as API contracts
- **IDE Support**: Full autocomplete and refactoring support

### Thin Service Layer
- Services coordinate between controllers and domain entities
- Business logic resides in domain entities, not services
- Constructor-based dependency injection throughout

### Domain Exceptions
- Specific exceptions for domain concepts (`TrackNotFoundException`, `InvalidTrackPositionException`)
- Global exception handler provides consistent API responses

## 🔧 Configuration

### Database Configuration
The application connects to MySQL using the configuration in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/spotify_api
spring.datasource.username=root
spring.datasource.password=secret
```

### Development Features
- SQL logging enabled for development
- Automatic schema updates via Hibernate
- Debug logging for application packages
- DTO validation with clear error messages

## 🎯 Modern Java 21 Features

### Records for DTOs
```java
// Type-safe, immutable request models
public record CreateTrackRequest(String title, String artist, Integer duration) {
    // Automatic validation in compact constructor
    public CreateTrackRequest {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        // ... additional validation
    }
}
```

### Benefits Over Map<String, Object>
- **Type Safety**: Compile-time validation vs runtime casting
- **Performance**: No reflection-based field access
- **Maintainability**: IDE support for refactoring
- **Documentation**: Self-documenting API contracts

## 📈 Future Enhancements

This architecture supports future extensions:
- Microservices decomposition (UUIDs enable distributed IDs)
- Event sourcing and CQRS patterns
- Domain events for cross-aggregate communication
- Additional rich domain behavior in entities
- Request/Response DTOs for complete API isolation
