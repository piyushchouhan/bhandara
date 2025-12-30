# Getting Started

## Prerequisites

- Android Studio (latest version)
- JDK 11 or higher
- Android SDK
- Git

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/piyushchouhan/bhandara.git
cd bhandara
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned repository
4. Click "OK"

### 3. Sync Gradle

Android Studio will automatically sync Gradle. If not:

1. Click on "File" → "Sync Project with Gradle Files"
2. Wait for the sync to complete

### 4. Run the Application

1. Connect an Android device or start an emulator
2. Click the "Run" button (▶️) in Android Studio
3. Select your target device
4. Wait for the app to build and install

## Configuration

### Google Services

The project uses Firebase. Ensure you have:

- Valid `google-services.json` in the `app/` directory
- Firebase project configured properly

### Local Properties

Check `local.properties` for SDK paths:

```properties
sdk.dir=/path/to/Android/sdk
```

## Troubleshooting

### Build Failures

- Clean the project: `Build → Clean Project`
- Rebuild: `Build → Rebuild Project`
- Invalidate caches: `File → Invalidate Caches / Restart`

### Gradle Issues

```bash
./gradlew clean
./gradlew build
```
