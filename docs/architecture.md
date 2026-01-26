# Architecture

## Overview

Bhandara follows modern Android development practices with clean architecture principles.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Build System**: Gradle (Kotlin DSL)
- **Backend**: 
  - Firebase (Auth, Firestore, FCM, Storage)
  - Custom REST API (Retrofit + OkHttp)
- **Background Jobs**: WorkManager
- **Maps**: Google Maps SDK for Android (Maps Compose)
- **Location**: Google Play Services Location API
- **Min SDK**: 35
- **Target SDK**: 36

## Firebase Integration

### Services Used

1. **Firebase Authentication** - Anonymous user sign-in
2. **Cloud Firestore** - User data storage (UID, FCM token, location)
3. **Cloud Messaging (FCM)** - Push notifications
4. **Cloud Storage** - Feast photo uploads

### Data Flow

```
User Opens App
    ↓
Anonymous Auth → UID
    ↓
FCM Token Generated
    ↓
Location Requested
    ↓
Save to Backend API & Firestore
```

## Project Structure

### Modules

- **app**: Main application module containing all source code and resources

### Key Directories

```
app/src/main/
├── java/com/example/bhandara/  # Application code
│   ├── MainActivity.kt          # Main entry point
│   ├── data/                    # Data layer
│   │   ├── api/                 # Retrofit services & interceptors
│   │   ├── models/              # Data models (User, Feast, API)
│   │   └── repository/          # Data access repositories
│   ├── managers/                # Business logic
│   │   └── UserManager.kt       # User initialization, tracking & sync
│   ├── services/                # Background services
│   │   └── BhandaraMessagingService.kt  # FCM receiver
│   ├── workers/                 # Background workers
│   │   └── LocationUpdateWorker.kt # Periodic location sync
│   ├── ui/                      # UI layer
│   │   ├── components/          # Reusable components
│   │   ├── screens/             # Screen composables
│   │   └── theme/               # Material theme
│   └── utils/                   # Utilities
│       └── LocationHelper.kt    # GPS & permissions
├── res/                         # Resources
│   ├── layout/                  # XML layouts (legacy)
│   ├── values/                  # Strings, colors, themes
│   ├── drawable/                # Images and icons
│   └── mipmap/                  # App icons
└── AndroidManifest.xml          # App manifest
```

## Key Components

### UserManager
- Initializes anonymous users on app start
- Manages FCM token lifecycle
- Coordinates manual and background location updates
- Syncs user data to Backend API

### BackendRepository
- Handles communication with the REST API
- Endpoints for creating users, updating location, and managing feasts

### LocationUpdateWorker
- Periodic background task (WorkManager)
- Updates user location to backend every 15 minutes when app is in background

### LocationHelper
- GPS location retrieval
- Permission management
- Location caching

### BhandaraMessagingService
- Receives FCM push notifications
- Displays system notifications
- Handles notification clicks

## Navigation

Simple enum-based navigation with back stack:

```kotlin
enum class Screen {
    HOME, HUNGRY, REPORT_BHANDARA
}
```

Back stack automatically manages navigation history for proper back button behavior.

## Dependencies

Dependencies are managed in `gradle/libs.versions.toml` using version catalogs.

### Key Libraries
- **Firebase BOM**: 34.7.0 (Auth, Firestore, Messaging, Storage)
- **Retrofit**: 2.11.0 (Networking)
- **WorkManager**: 2.9.0 (Background jobs)
- **Maps Compose**: 4.3.3 (Map UI)
- **Coil**: Image loading

See [Firebase Setup](firebase-setup.md) for detailed explanation of each dependency.

## Build Configuration

- **Project-level**: `build.gradle.kts` (root)
- **App-level**: `app/build.gradle.kts`
- **Gradle properties**: `gradle.properties`
- **Firebase config**: `app/google-services.json`

## Design Patterns

### Repository Pattern
- Separates data access from business logic
- `BackendRepository` handles API operations
- `UserRepository` handles Firebase operations

### Manager Pattern
- `UserManager` coordinates multiple operations
- Centralizes business logic
- Reduces MainActivity complexity

## Data Flow

### User Initialization
```
MainActivity
    ↓
UserManager.initializeUser()
    ↓
UserRepository.signInAnonymously() → Firebase Auth
    ↓
UserRepository.getFcmToken() → Firebase Messaging
    ↓
UserManager.updateUserLocation()
    ↓
BackendRepository.createUser() → API
```

### Notification Flow
```
Backend API
    ↓
Firebase Cloud Messaging
    ↓
BhandaraMessagingService.onMessageReceived()
    ↓
Create Notification
    ↓
System Notification Tray
    ↓
User clicks → Opens MainActivity
```

## Future Architecture

- **Offline Support**: Local Room database for caching feasts
- **Social Features**: Comments and ratings for bhandaras
- **Improved Maps**: Clustering and route navigation
