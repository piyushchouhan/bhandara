# Mobile–Backend Integration

## Source locations

The mobile REST wrappers live in `shared/src/commonMain/kotlin/com/example/bhandara/data/network/`. The companion `be-bhandara` repository implements the service under `src/main/java/com/localfeast/backend/`.

This page explains how the apps use the service. For server implementation, use the [backend overview](backend/index.md), [controller reference](backend/api-reference.md), and [authorization rules](backend/security.md).

## Transport and identity

`ApiClient` uses Ktor and kotlinx.serialization, with OkHttp on Android and Darwin on iOS. It sets JSON content type by default, ignores unknown response fields, and returns `ApiResult` values. Connection timeout is 15 seconds; request timeout is 60 seconds.

Android reads `BuildConfig.API_BASE_URL` from `app/build.gradle.kts`. iOS reads `ApiBaseUrl` from generated `Info.plist`, populated through `Secrets.xcconfig`. Use a backend URL reachable from the device and preserve the trailing slash expected by client configuration.

Requests attach a Firebase ID token when available. Anonymous Firebase sign-in provides an authenticated UID; a Google sign-in gate is an additional mobile interface rule. The server verifies tokens and checks ownership independently. Configured administrator emails in the mobile app affect prompts, while server `AdminAccess` checks verified Firebase email claims.

## Discovery and location

The home map requests `api/feed` with `lat`, `lon`, and `radius`. The backend sources stationary shops, live carts, and verification candidates, hydrates records from PostgreSQL, filters and ranks them, then returns `activeShops`, optional `verificationPrompt`, and `zones`.

The mobile map searches within returned data. Server zones provide membership, outlines, centers, and localized names. Moving feed candidates use live Redis positions rather than their original listing coordinates. The server's feed time filter hides moving candidates at or after 21:00 in the configured app time zone.

Feasts use `api/feasts/nearby` with `lat`, `lon`, and `radius`. Mobile defaults to the selected search radius, initially 5 km; the controller default when radius is omitted is 500 m. Do not rely on matching defaults across endpoints.

## Photos and AI extraction

Ordinary contribution photos are compressed and uploaded through platform Firebase Storage adapters. The backend receives download URLs with shops, feasts, reviews, claims, and report evidence. `PhotoUploader` reports batch progress and attempts cleanup of completed uploads on failure.

Menu extraction sends compressed image bytes directly to `api/menu-items/image/extract` as multipart `images` parts. The server allows up to ten image parts and returns draft items; the client lets the user inspect them before a separate save. Gemini assessment for ownership claims fetches permitted Firebase Storage URLs rather than arbitrary remote image hosts.

The mobile request timeout and upstream AI timeout are independent. A timeout does not mean every upstream operation was canceled; the client must not infer that a record was saved from an extraction result alone.

## Saved-place synchronization

`SavedPlacesSync` keeps local saves/removals queued and submits them to `api/saved/sync`. The server processes removals before additions and returns its full list, capped at 500 places. Guest saves can carry into a signed-in account; established account switches clear local state before loading that account's saves.

Keep the existing queue/merge logic when modifying this flow. It protects changes made during a request and avoids transferring one account's saved places to another.

## Live-cart protocol

Native STOMP clients connect to `/ws/websocket` and provide the bearer token in CONNECT headers when publishing.

| Destination | Direction | Purpose |
| --- | --- | --- |
| `/app/cart/location` | Android vendor → server | Publish position/presence |
| `/app/cart/offline` | Android vendor → server | Remove live cart immediately |
| `/topic/cart/{shopId}` | Server → customer | Stream accepted positions |

The server authorizes the stored cart owner, refreshes presence for 120 seconds, and broadcasts updates. Shared `LiveCartFeed` reconnects with increasing delays capped at 30 seconds. Its subscription closes when the tracking sheet is dismissed. Android vendor mode has a separate foreground publishing service; iOS implements customer viewing only.

The server also implements REST follow-once registrations and a `cart_proximity` push. Those endpoints and notification routing are not integrated into the inspected shared mobile client.

## Notifications

Android registers and synchronizes FCM tokens, submits location for nearby alerts, and routes `new_localfeast`, `report_update`, `shop_review`, and `claim_status` messages. New-feast targeting uses 500 m around the feast. Server `UserNotifier` enforces category preferences and optional 22:00–07:00 quiet hours for its supported categories.

The one-time cart proximity processor uses a different direct send path and currently bypasses that preference gate. iOS lacks equivalent push-delivery integration in the inspected host.

## Privacy and deletion

Mobile JSON export combines the current account details, selected API responses, and local saves. It is generated in the client, not through a dedicated backend export endpoint.

Account deletion invokes `DELETE api/users/me`. The backend transaction deletes database personal records and retains shops/feasts under `deleted-user` with specified contact/audit fields cleared. It then attempts Firebase identity deletion after commit. `Feast.organizerName`, stored media objects, and Redis follow registrations are not explicitly cleared by that service. See [data cleanup boundaries](roadmap.md#data-cleanup-boundaries) before changing the interface's deletion description.

## Error handling

The server advice returns `message`, `statusCode`, and optional field `errors` for many failures. Some controller and security paths return other shapes or empty bodies. `ApiClient` maps network failures, validation, authentication/authorization, missing resources, conflicts, rate limits, server failures, and unreadable successful responses. Coroutine cancellation propagates.

For route coverage, compare the [mobile wrappers](api-reference.md) with the [server route inventory](backend/api-reference.md). Existing server-only capabilities include shop updates, detailed menu item update/deletion, heatmap reads, one-time follows, and administrator operations.
