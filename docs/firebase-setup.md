# Firebase Setup & User Tracking

This guide explains how the app implements silent user tracking using Firebase Anonymous Authentication, Cloud Messaging (FCM), and location services.

## Overview

The app automatically tracks users without any login or signup. When a user installs and opens the app:

1. **Anonymous Authentication** - Assigns a permanent UID
2. **FCM Token** - Enables push notifications
3. **Location Tracking** - Gets user's current location
4. **Data Storage** - Saves to Firebase Firestore

All of this happens **silently in the background** without user interaction.

## Architecture

```
App Launch
    ↓
UserManager.initializeUser()
    ↓
Firebase Anonymous Auth → UID
    ↓
Firebase Messaging → FCM Token
    ↓
Save to Firestore (uid, fcmToken)
    ↓
Request Location Permissions
    ↓
Get Location → Update Firestore
```

## Implementation Details

### 1. Firebase Dependencies

Added to [app/build.gradle.kts](../app/build.gradle.kts):

```kotlin
// Firebase - Import the BoM
implementation(platform("com.google.firebase:firebase-bom:34.7.0"))

// Firebase products
implementation("com.google.firebase:firebase-analytics")
implementation("com.google.firebase:firebase-auth")           // Anonymous auth
implementation("com.google.firebase:firebase-firestore")      // User data storage
implementation("com.google.firebase:firebase-storage")        // Photo storage
implementation("com.google.firebase:firebase-messaging")      // Push notifications

// Google Play Services for Location
implementation("com.google.android.gms:play-services-location:21.3.0")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

**Why each dependency:**
- **firebase-auth**: Anonymous user authentication
- **firebase-firestore**: Store user data (UID, FCM token, location)
- **firebase-storage**: Future use for feast photos
- **firebase-messaging**: Receive push notifications
- **play-services-location**: Get user's GPS location
- **coil**: Load images efficiently in Compose

### 2. Android Manifest Permissions

Added to [AndroidManifest.xml](../app/src/main/AndroidManifest.xml):

```xml
<!-- Internet permission for Firebase -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- Location permissions for tracking user location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Notification permission (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Why each permission:**
- **INTERNET**: Firebase requires network access
- **ACCESS_FINE_LOCATION**: Get precise GPS coordinates
- **ACCESS_COARSE_LOCATION**: Fallback for approximate location
- **POST_NOTIFICATIONS**: Required for Android 13+ to show notifications

### 3. Firebase Cloud Messaging Service

Added [BhandaraMessagingService.kt](../app/src/main/java/com/example/bhandara/services/BhandaraMessagingService.kt):

```xml
<!-- In AndroidManifest.xml -->
<service
    android:name=".services.BhandaraMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**Purpose:**
- Receives push notifications from Firebase
- Displays notifications in system tray
- Handles notification clicks to open the app

**How it works:**
1. Backend sends FCM notification
2. Firebase delivers to device
3. Service receives message
4. Creates and displays notification

## Components

### UserManager

Location: [UserManager.kt](../app/src/main/java/com/example/bhandara/managers/UserManager.kt)

Responsible for:
- Initializing anonymous users
- Managing FCM tokens
- Updating user locations
- (Future) Syncing data to backend API

**Key Methods:**
- `initializeUser()`: Signs in anonymously and saves to Firestore
- `updateUserLocation()`: Gets GPS and updates Firestore

### UserRepository

Location: [UserRepository.kt](../app/src/main/java/com/example/bhandara/data/repository/UserRepository.kt)

Handles Firebase operations:
- `signInAnonymously()`: Creates anonymous Firebase user
- `getFcmToken()`: Retrieves FCM device token
- `saveUser()`: Saves user data to Firestore
- `updateUserLocation()`: Updates location in Firestore

### LocationHelper

Location: [LocationHelper.kt](../app/src/main/java/com/example/bhandara/utils/LocationHelper.kt)

Manages location services:
- `hasLocationPermissions()`: Checks if permissions granted
- `getCurrentLocation()`: Gets precise GPS coordinates
- `getLastKnownLocation()`: Gets cached location (faster)

### Data Models

**User Model** - [User.kt](../app/src/main/java/com/example/bhandara/data/models/User.kt):

```kotlin
data class User(
    val uid: String = "",
    val fcmToken: String = "",
    val location: GeoPoint? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
)
```

## User Flow

### First App Launch

1. **App Opens** → `MainActivity.onCreate()`
2. **UserManager Initialized** → Creates UserManager instance
3. **initializeUser() Called**:
   ```kotlin
   // Step 1: Anonymous sign in
   val uid = userRepository.signInAnonymously() // e.g., "abc123def456"
   
   // Step 2: Get FCM token
   val fcmToken = userRepository.getFcmToken() // e.g., "fcm_device_token_..."
   
   // Step 3: Save to Firestore
   userRepository.saveUser(uid, fcmToken)
   ```

4. **Permissions Requested** → Location and notifications
5. **Location Updated** (if granted):
   ```kotlin
   val location = locationHelper.getCurrentLocation()
   userRepository.updateUserLocation(uid, location)
   ```

### Subsequent App Launches

1. **App Opens**
2. **Existing User Check** → Firebase recognizes returning user
3. **Same UID Used** → No new user created
4. **FCM Token Verified** → Updated if changed
5. **Location Updated** → Fresh GPS coordinates

## Firebase Console Verification

### Authentication

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select "bhandara" project
3. Navigate to **Authentication** → **Users**
4. See anonymous users with UID

### Firestore Database

1. In Firebase Console
2. Navigate to **Firestore Database**
3. Open **users** collection
4. View documents with structure:
   ```
   users/
     {uid}/
       - uid: "abc123def456"
       - fcmToken: "fcm_token_..."
       - location: GeoPoint(18.5823749, 73.8844745)
       - createdAt: 1704045600000
       - lastActive: 1704045600000
   ```

### Cloud Messaging

1. In Firebase Console
2. Navigate to **Cloud Messaging**
3. View registered FCM tokens
4. Test notifications from console

## Debugging

### LogCat Tags

Monitor these tags in Android Studio Logcat:

- `UserManager`: User initialization flow
- `UserRepository`: Firebase operations
- `LocationHelper`: Location updates
- `BhandaraMessaging`: Notification handling

### Expected Log Sequence

```
UserManager: User signed in with UID: abc123def456
UserManager: FCM token obtained
UserRepository: User data saved successfully
UserManager: User saved to Firestore
LocationHelper: Location obtained: 18.5823749, 73.8844745
UserRepository: User location updated
UserManager: User location updated in Firestore
```

## Common Issues

### Location Not Updating

**Problem**: Location stays null in Firestore

**Solutions**:
- Check permissions granted
- Ensure emulator has location set (Extended Controls → Location)
- Use device with GPS instead of emulator
- Check Play Services installed on emulator

### FCM Token Not Generated

**Problem**: fcmToken is empty

**Solutions**:
- Use emulator with Google Play (not just Google APIs)
- Check `google-services.json` is valid
- Verify internet connection
- Restart app

### Anonymous User Not Created

**Problem**: UID is null

**Solutions**:
- Check Firebase Authentication is enabled
- Verify `google-services.json` configuration
- Enable Anonymous sign-in in Firebase Console

## Security Considerations

### Anonymous Auth Limitations

- **Device-specific**: UID tied to app installation
- **Non-recoverable**: Uninstall = new user
- **No cross-device**: Can't sync across devices

### Privacy

- No personal information collected
- Only: UID, FCM token, location
- Users can't be identified
- Complies with anonymous usage

### Data Retention

Consider implementing:
- Remove users inactive for 90+ days
- Clear location data older than 30 days
- GDPR compliance for EU users

## Next Steps

See [Backend Integration](backend-integration.md) for:
- Syncing user data to PostgreSQL/PostGIS
- Implementing backend API calls
- Querying nearby users
- Sending targeted notifications

## Related Documentation

- [Getting Started](getting-started.md) - Initial setup
- [Architecture](architecture.md) - Overall app structure
- [Backend Integration](backend-integration.md) - API implementation
- [Development Guide](development.md) - Best practices
