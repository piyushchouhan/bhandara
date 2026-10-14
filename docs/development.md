# Development Guide

## Work within the shared architecture

Place platform-independent screens, state, validation, API calls, and models in `shared/src/commonMain`. Use `androidMain` and `iosMain` for platform implementations. Android services belong in `app`; Swift SDK bridges belong in `iosApp`.

Feature ViewModels expose `StateFlow` state and use coroutines for asynchronous work. Continue the existing `ApiResult` error-handling pattern and preserve coroutine cancellation. Add API models and typed service wrappers rather than embedding requests in composables.

Use shared string resources for user-facing text and maintain the English, Hindi, and Marathi resource sets when adding or changing copy. Check theme and text-size behavior for new screens.

## Dependency management

The version catalogue lives in `gradle/libs.versions.toml`. Android and shared Compose versions must stay compatible; the catalogue documents this coupling. Native iOS dependencies are configured in `iosApp/project.yml`.

Use the checked-in wrapper. The Gradle runtime needs a compatible JDK; the project's JVM bytecode target of 11 does not mean JDK 11 is sufficient to run the Android build.

## Build and test

Run checks relevant to the change:

```bash
# Shared tests on the Android JVM target
./gradlew :shared:testDebugUnitTest

# Android host unit tests and debug build
./gradlew :app:testDebugUnitTest :app:assembleDebug

# Shared tests on an Apple Silicon iOS simulator
./gradlew :shared:iosSimulatorArm64Test

# Android device/emulator tests, when a device is connected
./gradlew :app:connectedDebugAndroidTest
```

The shared suite covers authentication, API outcomes, location, live feeds, saved-place synchronization, preferences, photos, navigation, and feature ViewModels. Ktor MockEngine and fake dependencies support deterministic checks without a production backend.

Native SDK integration, maps, camera access, push delivery, and long-running vendor tracking also require device-level validation. A passing unit suite does not establish production acceptance.

## Validate real-world behavior

For location-related changes, check approximate and precise permission states, location services being disabled, app backgrounding, and network recovery. For authentication and sync changes, check guest-to-account transitions, sign-out, and switching accounts. For contribution flows, check failed uploads and rejected requests before a success state is shown.

Keep ownership and administrative authorization on the backend. Configured administrator emails influence interface prompts and are not an authorization boundary.

## Release configuration

The current Android build configuration uses a local development endpoint and a debug keystore for release builds. Configure the intended backend, production transport policy, application identity, and proper signing before distribution. The iOS environment is supplied through its ignored xcconfig file and native Firebase configuration.

Generated outputs, credentials, signing material, and local environment files do not belong in source control. Deployment and signing changes should be reviewed as release work.

## Documentation workflow

The site uses MkDocs Material. Documentation source is in `docs/`, navigation and theme are in `mkdocs.yml`, and generated HTML is ignored under `site/`.

```bash
# Existing environment
source venv/bin/activate
mkdocs serve

# Validate links and configuration warnings during a documentation build
mkdocs build --strict
```

If the environment is missing:

```bash
python3 -m venv venv
source venv/bin/activate
python -m pip install mkdocs-material
```

When publication is intended, deploy from the source branch:

```bash
mkdocs gh-deploy
```

This updates the documentation hosting branch. Do not edit generated pages manually. A local build does not publish the site.

When product behavior changes, update the feature catalogue, platform coverage, and known limitations as well as technical details. Investor-facing statements must distinguish verified product facts, proposed priorities, and business hypotheses. Add commercial metrics only from verified records, with dates and definitions.
