# Backend Integration

## Overview

The application communicates with a backend REST API to handle user synchronization, location updates, and bhandara (feast) management. The networking layer is built using **Retrofit** and **OkHttp**, ensuring robust data exchange with proper authentication and error handling.

## Mobile Implementation

### Tech Stack

- **Retrofit 2**: Type-safe HTTP client.
- **OkHttp 3**: Underlying HTTP client with interceptors.
- **Gson**: JSON serialization/deserialization.
- **Coroutines**: Asynchronous API calls.

### Network Configuration

The network configuration is centralized in `com.example.bhandara.data.api.NetworkModule`.

- **Base URL**: Defined in `build.gradle.kts` as `API_BASE_URL`.
  - *Debug*: Defaults to `http://192.168.1.4:8080/` (Local Network)
  - *Release*: Should point to production server.
- **Timeouts**: 30 seconds for connect, read, and write operations.
- **Logging**: `HttpLoggingInterceptor` is enabled in DEBUG mode (Level: BODY).

### Authentication

Security is handled via `AuthInterceptor`. It automatically retrieves the current Firebase User's ID token and attaches it to every request:

```http
Authorization: Bearer <firebase_id_token>
```

If the user is not signed in (or `signInAnonymously` hasn't completed), the request proceeds without the header.

## API Reference

The contract is defined in `com.example.bhandara.data.api.ApiService`.

### 1. User Management

#### Create User
Registers a new user (anonymous or authenticated) with the backend.

- **Endpoint**: `POST api/users`
- **Request**: `CreateUserRequest`
  - `uid`: Firebase User ID
  - `fcmToken`: Firebase Cloud Messaging token
  - `latitude`: Current latitude
  - `longitude`: Current longitude
- **Response**: `CreateUserResponse`

#### Update Location
Periodically updates the user's location for geo-fencing features.

- **Endpoint**: `PUT api/users/location`
- **Request**: `UpdateLocationRequest`
  - `uid`: Firebase User ID
  - `latitude`: New latitude
  - `longitude`: New longitude
- **Response**: `UpdateLocationResponse`

### 2. Feast Management

#### Report Bhandara (Create Feast)
Submits a new bhandara event.

- **Endpoint**: `POST api/feasts`
- **Request**: `FeastRequest`
  - `menuItems`: List of food items
  - `startTime` / `endTime`: Event duration
  - `latitude` / `longitude`: Event location
  - `imageUrls`: List of uploaded image URLs
  - (and other details)
- **Response**: `FeastResponse`

#### Get Nearby Feasts
Retrieves active bhandaras within a specific radius.

- **Endpoint**: `GET api/feasts/nearby`
- **Query Params**:
  - `lat`: User's latitude
  - `lon`: User's longitude
  - `radius`: Search radius in meters (default: 500.0)
- **Response**: `List<FeastResponse>`

## Backend Requirements (Reference)

For the server-side implementation to support this mobile app, the following database structure (using PostgreSQL + PostGIS) is recommended.

### Database Schema

```sql
CREATE EXTENSION postgis;

-- Users Table
CREATE TABLE users (
    uid VARCHAR(128) PRIMARY KEY,
    fcm_token VARCHAR(512),
    location GEOGRAPHY(POINT, 4326),
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Feasts Table
CREATE TABLE feasts (
    id SERIAL PRIMARY KEY,
    organizer_uid VARCHAR(128) REFERENCES users(uid),
    description TEXT,
    location GEOGRAPHY(POINT, 4326),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_location ON users USING GIST (location);
CREATE INDEX idx_feasts_location ON feasts USING GIST (location);
```

### API Implementation Notes

1.  **Duplicate Handling**: The `POST api/users` endpoint should handle "upsert" logic (insert if new, update if exists).
2.  **Geo-Queries**: Use PostGIS `ST_DWithin` for the `nearby` endpoint to efficiently find feasts.
3.  **Token Validation**: The backend **must** verify the Firebase ID token in the `Authorization` header using the Firebase Admin SDK.
