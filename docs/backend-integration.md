# Backend Integration

## Scope and ownership

The mobile applications use a custom REST backend and STOMP/WebSocket service. This repository contains the client integration, not the server implementation, database migrations, moderator console, or deployment configuration.

The descriptions below reflect the client contract as reviewed on **14 October 2026**. Confirm authorization, retention, moderation, rate limits, and operational guarantees against the actual backend before making production claims.

## REST transport

`shared/.../data/network/ApiClient.kt` uses Ktor with kotlinx.serialization. Android uses an OkHttp engine; iOS uses Darwin. The client sets JSON content type by default, ignores unknown response fields, and maps unsuccessful responses to structured `ApiError` values.

- Connection timeout: **15 seconds**.
- Request timeout: **60 seconds**, including menu-photo extraction.
- Authentication: `Authorization: Bearer <Firebase ID token>` when available.
- Android base URL: `BuildConfig.API_BASE_URL` from `app/build.gradle.kts`.
- iOS base URL: `ApiBaseUrl` from the generated `Info.plist`, populated through `Secrets.xcconfig`.

Anonymous Firebase users also have an authenticated identity. The client may send a request without a token when token retrieval is unavailable; the backend must reject protected operations appropriately.

## Main data flows

### Discovery

The shops map requests `api/feed` using `lat`, `lon`, and `radius` query parameters. The response supplies active shops/carts, an optional verification prompt, and optional shop zones. Zones include shop IDs, center coordinates, outlines, and localized names.

Feast discovery uses `api/feasts/nearby` with `lat`, `lon`, and radius. Search radii are expressed in meters; the user's default is 5 km.

### Contributions and media

Photos are compressed and uploaded through platform Firebase Storage adapters. The client submits resulting URLs with shops, feasts, reviews, claims, or report evidence. `PhotoUploader` reports progress and attempts cleanup of completed uploads when a batch fails.

Menu extraction is different: compressed photos go directly to `api/menu-items/image/extract` as multipart data under the `images` field. Returned draft items are checked and edited before menu submission.

### Saved places

`api/saved/sync` accepts queued saves and removals and returns the account's saved places. The client persists changes locally until synchronization succeeds. Account switching and guest migration are coordinated by `SavedPlacesSync`.

### Accounts and notifications

Android registers users and FCM tokens, synchronizes replacement tokens, and submits location for nearby notification targeting. Notification preferences have a backend record and a local copy. The source includes notification routing for new feasts, report updates, shop reviews, and claim outcomes.

The account deletion client expects personal account data to be removed while shops and feasts remain without the person's name. This is an integration expectation; backend execution and retention need independent verification.

## WebSocket integration

The base URL is converted from HTTP(S) to WS(S), with `/ws/websocket` appended. Firebase tokens are supplied in STOMP connection headers.

| Destination | Direction | Purpose |
| --- | --- | --- |
| `/app/cart/location` | Android vendor → server | Publish position and presence |
| `/app/cart/offline` | Android vendor → server | Signal that sharing stopped |
| `/topic/cart/{shopId}` | Server → customer | Deliver cart position updates |

Shared `LiveCartFeed` reconnects with increasing delays capped at 30 seconds and skips unreadable messages. The Android vendor publisher has its own connection and retry handling. The server must authorize publishing against actual ownership rather than trusting identity fields in a message.

## Trust and moderation boundaries

The client supports ownership claim outcomes, verification votes with coordinates, report evidence, report status, review visibility states, and feast takedown/resubmission states. It does not prove how the server reaches those decisions.

Server responsibilities include token validation, owner/admin authorization, proximity enforcement, duplicate prevention, rate limiting, claim decisions, moderation actions, and notification fan-out. Configured client administrator email lists are for interface behavior only.

## Failure handling

The client maps validation errors, unauthorized access, forbidden operations, missing resources, conflicts, rate limits, service failures, network failures, and unreadable responses. Validation messages can be shown to the user; internal failures receive generic messages. Cancellation propagates when a screen or operation is stopped.

For exact paths and payload families, continue to the [API reference](api-reference.md).
