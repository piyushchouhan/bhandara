# Firebase and Platform Services

## Service roles

| Service | Android | iOS |
| --- | --- | --- |
| Firebase Authentication | Anonymous identity and Google account sign-in | Native Swift bridges for anonymous identity and Google account sign-in |
| Firebase Storage | Photo uploads through `AndroidFirebaseStorage` | Photo uploads through `IosFirebaseStorage` |
| Firebase Cloud Messaging | Token synchronization, incoming notifications, and tap routing | No delivery integration in inspected source |
| Firebase Analytics | Dependency included | Not configured in the inspected host |
| Google Maps | Native SDK through Maps Compose | Native SDK through `IosGoogleMapBridge` |

Firestore is not part of the current mobile persistence implementation. Product records and synchronization use the custom backend; local preferences and saved-place state use multiplatform settings. An Analytics dependency alone does not establish a validated event or business metrics programme.

## Configure authentication

Enable anonymous authentication and Google sign-in in the intended Firebase project. Register the Android application and iOS bundle using their configured identifiers.

For Android, place `google-services.json` in `app/`, configure the relevant signing certificate fingerprints, and verify the OAuth client configuration. For iOS, place `GoogleService-Info.plist` in `iosApp/iosApp/` and set `GOOGLE_REVERSED_CLIENT_ID` in `Secrets.xcconfig`.

`AuthService` coordinates anonymous sign-in and token access. Platform Google sign-in implementations handle account authentication. Protected server operations must validate Firebase tokens independently; a Firebase UID is an identifier, not proof of authorization.

## Configure photo storage

Enable the Storage service used by the app and review access rules for the intended authentication and upload model. Both platform adapters use the shared uploader; its default folder is `feast_images`, including photos contributed to multiple feature types.

Verify successful upload, failed upload, canceled selection, and user-facing progress. The client attempts cleanup of uploads completed earlier in a failed batch; complete orphan cleanup and retention policy require service-side review.

## Configure maps

Enable the appropriate Maps SDK for each platform. Android reads `MAPS_API_KEY` from `local.properties`; iOS reads its key from `Secrets.xcconfig` through generated `Info.plist` settings. Restrict each key for the intended application and SDK.

## Android permissions and background behavior

The manifest declares internet, fine/coarse location, notification, camera, foreground service, and foreground location service permissions. The camera is optional hardware.

Location and notification access use runtime permission flows. Approximate location can support discovery, although accuracy affects proximity-sensitive operations. Denied permissions and disabled location services have explicit states in shared location flows.

Two distinct mechanisms use location:

- **User-location synchronization:** `UserManager` can schedule WorkManager at a 15-minute interval for nearby notification targeting. Execution timing and location availability depend on Android's permission and background restrictions; the manifest does not declare background-location permission.
- **Vendor sharing:** an explicitly enabled foreground location service publishes the cart's position with a visible ongoing notification. Turning vendor mode off stops sharing through this service.

Shared crowd pings run only while the app is visible and only when a permitted location is near a fixed shop. This is separate from vendor tracking.

## Android notifications

`BhandaraMessagingService` handles incoming FCM messages and token changes. `PushTokenSync` records successful token synchronization so a failed send can be retried at a later app start.

Notification payloads support `new_localfeast`, `report_update`, `shop_review`, and `claim_status`. The Android host maps these to the corresponding app routes. Backend delivery must respect the user's notification preferences and quiet-hours settings; the mobile settings interface alone does not verify that enforcement.

## iOS permissions and limits

The XcodeGen configuration includes camera and location usage descriptions, Google sign-in URL handling, and native maps/storage setup. The photo library uses the system picker. The host requests location for use while the app is active.

There is no equivalent iOS FCM delivery setup, periodic user-location worker, or vendor foreground publishing service in the inspected source. Shared notification preferences and cart viewing do not imply those native integrations exist.

## Privacy and operational verification

Anonymous authentication is pseudonymous: account IDs, location, push tokens, and contributions can still be sensitive. Google sign-in also provides account information. Do not describe these flows as collecting no personal data or as automatically satisfying a legal standard.

The app includes selective JSON export and an account deletion request. Verify the backend's actual deletion, retained contributions, storage cleanup, and retention behavior before publishing binding privacy promises. See [feature catalogue](features.md) for user-facing scope and [backend integration](backend-integration.md) for service responsibilities.
