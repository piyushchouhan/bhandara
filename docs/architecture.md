# Architecture

## Overview

Bhandara follows modern Android development practices with clean architecture principles.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Build System**: Gradle (Kotlin DSL)
- **Backend**: Firebase (Auth, Firestore, FCM, Storage)
- **Location**: Google Play Services Location API
- **Min SDK**: 35
- **Target SDK**: 36

## Firebase Integration

### Services Used

1. **Firebase Authentication** - Anonymous user sign-in
2. **Cloud Firestore** - User data storage (UID, FCM token, location)
3. **Cloud Messaging (FCM)** - Push notifications
4. **Cloud Storage** - Future: Feast photo uploads

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
Save to Firestore
    ↓
(Future) Sync to Backend API
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
│   │   ├── models/              # Data models (User, Feast)
│   │   └── repository/          # Firebase operations
│   ├── managers/                # Business logic
│   │   └── UserManager.kt       # User initialization & tracking
│   ├── services/                # Background services
│   │   └── BhandaraMessagingService.kt  # FCM receiver
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
- Handles location updates
- Coordinates data sync to Firestore and backend

### UserRepository
- Firebase Authentication operations
- Firestore CRUD operations
- FCM token management

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
    HOME, HUNGRY, REPORT_FEAST
}
```

Back stack automatically manages navigation history for proper back button behavior.

## Dependencies

Dependencies are managed in `gradle/libs.versions.toml` using version catalogs.

### Firebase Dependencies

Added in [app/build.gradle.kts](../app/build.gradle.kts):

```kotlin
implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-storage")
implementation("com.google.firebase:firebase-messaging")
```

See [Firebase Setup](firebase-setup.md) for detailed explanation of each dependency.

## Build Configuration

- **Project-level**: `build.gradle.kts` (root)
- **App-level**: `app/build.gradle.kts`
- **Gradle properties**: `gradle.properties`
- **Firebase config**: `app/google-services.json`

## Design Patterns

### Repository Pattern
- Separates data access from business logic
- `UserRepository` handles all Firebase operations
- Makes testing easier

### Manager Pattern
- `UserManager` coordinates multiple operations
- Centralizes business logic
- Reduces MainActivity complexity

### Single Responsibility
- Each class has one clear purpose
- LocationHelper only handles location
- Services only handle their specific task

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
UserRepository.saveUser() → Firestore
    ↓
LocationHelper.getCurrentLocation() → Play Services
    ↓
UserRepository.updateUserLocation() → Firestore
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

### Backend Integration
- API calls via Retrofit
- PostgreSQL + PostGIS for geo queries
- Backend sends FCM notifications

See [Backend Integration](backend-integration.md) for implementation details.
