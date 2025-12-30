# Welcome to Bhandara Documentation

Bhandara is an Android application built with Kotlin that helps users discover and report community feasts (Bhandaras). The app uses Firebase for user tracking and notifications, with location-based features to notify nearby users.

## Quick Start

To get started with the project:

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Configure Firebase (see [Firebase Setup](firebase-setup.md))
5. Run the app on an emulator or device

## Key Features

- **Anonymous User Tracking** - No login required, automatic UID assignment
- **Location-Based Services** - GPS tracking for nearby feast notifications
- **Push Notifications** - FCM integration for real-time alerts
- **Silent Data Collection** - Background tracking of user location and tokens

## Project Structure

```
bhandara/
├── app/                    # Main application module
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Kotlin source files
│   │       │   ├── data/   # Data models & repositories
│   │       │   ├── managers/ # Business logic managers
│   │       │   ├── services/ # Background services
│   │       │   ├── ui/     # UI components & screens
│   │       │   └── utils/  # Helper utilities
│   │       └── res/        # Resources
│   ├── build.gradle.kts    # App-level Gradle config
│   └── google-services.json # Firebase configuration
├── gradle/                 # Gradle wrapper and dependencies
├── docs/                   # Documentation (MkDocs)
└── build.gradle.kts        # Project-level Gradle config
```

## Documentation Sections

- [Getting Started](getting-started.md) - Setup and installation
- [Firebase Setup](firebase-setup.md) - **User tracking, authentication & notifications**
- [Architecture](architecture.md) - App architecture and design
- [Features](features.md) - Application features
- [Development](development.md) - Development guidelines
- [Backend Integration](backend-integration.md) - API sync with PostgreSQL/PostGIS
- [API Reference](api-reference.md) - Code documentation

## Commands

* `mkdocs serve` - Start the live-reloading docs server
* `mkdocs build` - Build the documentation site
* `mkdocs gh-deploy` - Deploy to GitHub Pages
