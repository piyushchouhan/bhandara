# Getting Started

## Prerequisites

Use the repository's Gradle wrapper and dependency catalogue rather than substituting independently selected library versions.

For Android, install an Android Studio version compatible with the configured Android Gradle Plugin, JDK 17 or a compatible Gradle runtime, and Android SDK 36. The application currently requires Android API 35 or later. The Java/Kotlin bytecode target is 11; that is separate from the JDK needed to run the build.

For iOS, use macOS, Xcode, XcodeGen, and a compatible JDK. Kotlin targets arm64 iPhones and Apple Silicon simulators. `iosApp/project.yml` declares iOS 16.0; verify deployment compatibility with the native SDK versions resolved by Swift Package Manager.

Both platforms require access to the backend, Firebase, Google Maps, and the relevant platform SDKs.

## Android setup

1. Open the repository root in Android Studio and allow Gradle to sync.
2. Place the appropriate Firebase Android configuration at `app/google-services.json`.
3. Set `sdk.dir` and `MAPS_API_KEY` in the ignored `local.properties` file.
4. Configure `API_BASE_URL` in `app/build.gradle.kts` for the backend reachable from the test device. The checked-in value points to a local network development host; it is not a production endpoint.
5. Enable the required Firebase sign-in providers and configure Google sign-in for the application and signing certificate.
6. Run on an API 35+ device or emulator with suitable Google services.

```properties
sdk.dir=/absolute/path/to/Android/sdk
MAPS_API_KEY=your_android_maps_key
```

Build a debug package:

```bash
./gradlew :app:assembleDebug
```

The configured Android application ID is `com.example.bhandara`. A physical device must be able to reach the selected backend host. For local testing, an emulator's host address and a physical device's network address are different; configure the URL accordingly.

## iOS setup

1. Place the matching Firebase configuration at `iosApp/iosApp/GoogleService-Info.plist`.
2. Copy `iosApp/Config/Secrets.xcconfig.example` to `iosApp/Config/Secrets.xcconfig`.
3. Fill in the iOS Maps key, backend base URL, and reversed Google client ID.
4. Generate the Xcode project and open it.

```bash
cd iosApp
cp Config/Secrets.xcconfig.example Config/Secrets.xcconfig
# Fill in Secrets.xcconfig before continuing.
xcodegen generate
open iosApp.xcodeproj
```

The Xcode build script invokes `:shared:embedAndSignAppleFrameworkForXcode`. The generated Xcode project and `Info.plist` are not the configuration source of truth; edit `project.yml` and regenerate.

The bundle identifier is `com.localfeast.app`. In an xcconfig file, `//` begins a comment: follow the example file's `https:/$()/api.example.com/` notation when setting the URL. Use a trailing slash for the base URL.

## First functional checks

Confirm map rendering, anonymous authentication, nearby discovery, and photo selection. Then exercise Google sign-in and an account-required flow such as adding or claiming a shop. Test with location allowed and denied, and verify that the interface provides a recoverable state when services are unavailable.

For Android, separately verify notification permission and vendor mode's foreground notification. iOS vendor publishing and push delivery are not implemented in the inspected source.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Gradle cannot start | Gradle runtime JDK and Android SDK configuration |
| Map is blank | Platform Maps SDK enablement, key restrictions, app ID/bundle ID, and network access |
| Google sign-in fails | Firebase provider settings, OAuth clients, Android signing fingerprints, or iOS reversed client ID |
| Nearby data is missing | Device location, selected radius, backend reachability, authentication, and available listings |
| Photo upload fails | Firebase Storage availability, access rules, permission state, and network access |
| iOS framework cannot be linked | Xcode build-script output, selected architecture, JDK, and generated project configuration |

See [Firebase and platform services](firebase-setup.md) and [backend integration](backend-integration.md) for configuration responsibilities. Local files containing environment settings should not be committed.
