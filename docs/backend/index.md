# Backend Overview

The `be-bhandara` repository implements the Local Feast service using **Java 21** and **Spring Boot 4.0.1**. It is organized as one application with feature modules, backed by PostgreSQL/PostGIS, Redis, and Meilisearch.

## Module map

All Java paths below are relative to `src/main/java/com/localfeast/backend/`.

| Package | Responsibility |
| --- | --- |
| `modules/user` | Registration, location, push tokens, notification preferences, and account deletion |
| `modules/localshops` | Shop/cart records, updates, flags, ownership enforcement, and community verification |
| `modules/feed` | Candidate sourcing, hydration, filtering, scoring, and feed selection |
| `modules/zones` | Spatial grouping, outlines, stored labels, and asynchronous AI naming |
| `modules/cart` | Live positions, Redis presence, STOMP broadcasting, and one-time proximity alerts |
| `modules/crowd` | Short-lived pings, crowd levels, heatmap aggregation, and cleanup |
| `modules/feast` | Posting, nearby discovery, editing, expiry, reports, and resubmission |
| `modules/menuitem` | Detailed menu creation, updates, removal, and price metadata |
| `modules/review` | Reviews, helpful votes, price-mention checks, and moderation |
| `modules/shopclaim` | Ownership claim assessment and administrator decisions |
| `modules/myreports` | Reporter history, follow-up evidence, withdrawal, and administrator report inspection |
| `modules/saved` | Account-scoped saved places and delta synchronization |
| `modules/alerts` | Event-driven report, review, and claim notifications with user preferences |
| `modules/ai` | Gemini transport, menu extraction, and claim image assessment |
| `modules/support` | Issue reports and feature suggestions |
| `common` | Security, configuration, validation, errors, clock, and legal page |
| `service` | Shared search, push, and Redis token-bucket services |

## Request processing

A typical REST request passes through Spring Security and Firebase token verification, then request validation, a controller, a domain service, and repositories. Methods annotated with `@RateLimit` also pass through the Redis-backed MVC rate-limit interceptor. Controllers return DTOs rather than persistence entities.

PostgreSQL is the persistent source of record. Meilisearch supplies candidate IDs; the feed hydrates them from PostgreSQL before filtering and ranking. Redis supplies current cart coordinates and ephemeral state. This distinction matters when troubleshooting stale search results or expired cart presence.

## Integration boundaries

Firebase Admin verifies mobile ID tokens and sends FCM notifications. Mobile clients upload ordinary contribution photos to Firebase Storage; the backend stores their URLs. Menu extraction receives multipart images, while claim assessment fetches permitted Firebase Storage URLs. Gemini also generates localized zone labels in the background.

The active cart event processor is local. `KafkaCartLocationUpdateProcessor` is a placeholder and the POM does not include Kafka support.

## Reading guide

Follow [architecture and flows](architecture.md) for runtime behavior, [data model](data-model.md) for storage, [API reference](api-reference.md) for routes, [authentication and moderation](security.md) for rules, and [operations](operations.md) for startup/configuration. [Testing](testing.md) describes the existing unit and container-backed integration setup.

Source baseline: backend revision `f3751bf`, reviewed on 14 October 2026. These pages describe code and configuration; they do not verify a running environment.
