# Architecture

## System overview

Local Feast uses Kotlin Multiplatform and Compose Multiplatform to share UI and application logic across Android and iOS. Native hosts provide platform services such as authentication SDKs, maps, photo storage, and device permissions.

```text
Android host (app/)                 iOS host (iosApp/)
Firebase, FCM, tracking             SwiftUI, Firebase, Google SDK bridges
                \                 /
                 Shared product layer
          Compose screens and navigation
             ViewModels and StateFlow
      Typed APIs, models, settings, and sync
                /                  \
       REST backend          STOMP/WebSocket service
                 Firebase photo storage
```

Backend services and their deployment are external to this repository. The architecture below describes the mobile implementation reviewed on 14 October 2026.

## Modules and responsibilities

| Location | Responsibility |
| --- | --- |
| `shared/src/commonMain` | Screens, ViewModels, navigation, API models/clients, authentication coordination, saved-place sync, crowd pings, themes, and preferences |
| `shared/src/androidMain` | Android implementations of maps, HTTP engine, location, settings, permissions, photo picking, and platform actions |
| `shared/src/iosMain` | iOS implementations and Compose entry point; interfaces to native Swift bridges |
| `app/src/main` | Android entry point, Firebase adapters, user registration, FCM, WorkManager, and vendor tracking |
| `iosApp/iosApp` | SwiftUI host and bridges for Firebase authentication/storage, Google sign-in, and Google Maps |
| `shared/src/commonTest` | Shared unit and integration-style tests using fake dependencies and Ktor MockEngine |

## UI and navigation

Feature packages group screens and their ViewModels: shops map, shop details, add shop, claims, menus, feasts, reviews, saved places, profile, reports, privacy, and settings.

ViewModels expose state through `StateFlow`. `AppDependencies` supplies the shared API client, authentication service, settings, photo uploader, app version, and configured administrator emails. Compose providers supply platform actions and the image loader.

`Route` is a serializable sealed interface. `AppNavigator` manages the back stack, while `NavEntries` maintains a ViewModel store and saved UI state per entry. Android delegates back handling to the host; iOS supports a left-edge back gesture. Android notification payloads map to routes through `NotificationRoutes`.

## Data and authentication

`ApiClient` uses Ktor, kotlinx.serialization, and a platform engine: OkHttp on Android and Darwin on iOS. Requests attach a Firebase ID token when available and return `ApiResult` values with mapped errors. The configured connection timeout is 15 seconds and request timeout is 60 seconds, allowing menu-photo extraction requests more time.

`AuthService` coordinates anonymous sign-in with a mutex to avoid duplicate concurrent initialization. Platform adapters provide token and account information. Google sign-in enables account-based actions; the backend remains responsible for authorization, ownership, and administrator checks.

## Real-time cart flow

1. Android vendor mode starts `VendorTrackingService` with a visible ongoing notification.
2. `VendorLocationManager` manages moving/parked location behavior and presence updates.
3. `CartStompClient` sends authenticated cart messages over STOMP.
4. A customer opens `LiveCartSheet`, which subscribes through shared `LiveCartFeed`.
5. Incoming positions update the cart map; reconnection uses exponential delays capped at 30 seconds.

The connection endpoint is `/ws/websocket`. Customer viewing is shared; vendor publishing is Android-specific.

## Persistence and synchronization

`AppSettings` and `AppPreferences` use multiplatform settings with platform storage. They keep preferences, saved-place IDs, contribution flags, vendor information, and listing drafts.

`SavedPlacesSync` sends queued saves/removals and merges the account response. It supports carrying guest saves into an account while avoiding transfer between established accounts. Lifecycle events, saved-place changes, and account changes trigger synchronization.

There is no Room-based discovery cache in the inspected implementation.

## Location and notifications

Shared location services support map discovery and contribution flows. `CrowdPinger` runs while the app is visible and sends proximity pings near a fixed shop; it checks every 45 seconds and uses a 50 m proximity threshold. It does not request permission itself.

Android `UserManager` registers the user and push token, coordinates location updates, and schedules periodic work at a 15-minute interval. Scheduling does not guarantee background location access or exact execution timing. Vendor sharing uses a distinct foreground service.

Android FCM delivery, token synchronization, and notification tap routing are implemented. The iOS host currently initializes authentication, storage, sign-in, and maps without an equivalent push delivery integration.

## Technology baseline

Versions are defined in `gradle/libs.versions.toml` and platform build files. The reviewed catalogue uses Kotlin 2.4.21, Compose Multiplatform 1.11.1, Ktor 3.5.2, and AGP 8.13.2. Android compiles and targets API 36 with minimum API 35. The iOS project declares deployment target 16.0 and builds arm64 device/simulator frameworks; validate the effective minimum against resolved native SDKs.

See [development](development.md) for validation commands and [backend integration](backend-integration.md) for service boundaries.
