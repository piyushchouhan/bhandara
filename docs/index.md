# Developer Documentation

Local Feast is a location-based application for discovering food shops, moving vendors, and community feasts. This site documents the Android and iOS applications in `bhandara` and the Java service in the companion `be-bhandara` repository.

## System components

| Component | Source | Responsibility |
| --- | --- | --- |
| Android host | `bhandara/app` | App entry point, native authentication/storage adapters, FCM, periodic work, and vendor tracking |
| Shared mobile layer | `bhandara/shared` | Compose screens, ViewModels, navigation, REST clients, models, preferences, and synchronization |
| iOS host | `bhandara/iosApp` | SwiftUI host and native SDK bridges |
| Backend | `be-bhandara/src/main/java/com/localfeast/backend` | REST endpoints, domain services, authorization, discovery, moderation, and real-time cart updates |
| PostgreSQL/PostGIS | Backend runtime dependency | Persistent records and spatial queries |
| Redis | Backend runtime dependency | Live cart positions, crowd pings, follows, and rate limiting |
| Meilisearch | Backend runtime dependency | Nearby shop candidate search, synchronized from PostgreSQL |
| Firebase and Gemini | External integrations | Authentication, mobile media storage, push delivery, menu extraction, claim assessment, and zone naming |

## Start here

- [Mobile setup](getting-started.md): platform prerequisites, local configuration, and builds.
- [Backend setup and operations](backend/operations.md): dependencies, environment variables, profiles, and startup.
- [Mobile architecture](architecture.md) and [backend architecture](backend/architecture.md): code organization and request processing.
- [Features](features.md): implemented workflows and platform coverage.
- [Mobile API reference](api-reference.md): endpoints consumed by shared clients.
- [Backend API reference](backend/api-reference.md): controller routes, including server-only and administrative operations.
- [Data model](backend/data-model.md): persistent entities, Redis structures, and synchronization.
- [Authentication and moderation](backend/security.md): identity, ownership, reports, and claim rules.
- [Development](development.md), [backend testing](backend/testing.md), and [implementation gaps](roadmap.md): maintenance and validation.

## Documentation scope

Reviewed on **14 October 2026** against the mobile working tree and backend revision `f3751bf`. Backend source was inspected locally; the backend repository was not modified. Configuration and implemented behavior are documented separately from deployment state. Application tests were not rerun for this documentation update.

Use Java controller annotations and service implementations as the authority when older comments disagree with code. In particular, user registration requires authentication, the discovery route is `/api/feed`, and shop crowd levels use a 50 m radius.
