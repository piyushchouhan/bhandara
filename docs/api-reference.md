# Mobile API Reference

## Reading this reference

This is the REST and real-time contract consumed by the mobile source, reviewed on **14 October 2026**. Paths are relative to the configured backend URL. For additional routes and controller implementations, see the [backend API reference](backend/api-reference.md).

Wrappers live under `shared/src/commonMain/kotlin/com/example/bhandara/data/network/`. Serializable domain payloads live under `data/models/api/`, with synchronization and preference payloads also defined beside their API wrappers.

## Users and discovery

| Method | Path | Purpose / payload |
| --- | --- | --- |
| POST | `api/users` | Register user with `CreateUserRequest` |
| PUT | `api/users/location` | Update user location with `UpdateLocationRequest` |
| PUT | `api/users/push-token` | Update `fcmToken` |
| GET | `api/users/notifications` | Read notification preferences |
| PUT | `api/users/notifications` | Change supplied notification preference fields |
| DELETE | `api/users/me` | Request account and personal-data deletion |
| GET | `api/feed` | Feed: `lat`, `lon`, `radius` |
| POST | `api/crowd/ping` | Proximity ping with `latitude`, `longitude` |
| POST | `api/saved/sync` | Submit `saved` and `removed` lists; return account saves |

`FeedResponse` contains `activeShops`, optional `verificationPrompt`, and `zones`. Saved entries use kind `SHOP` or `FEAST`, string `id`, and optional `savedAt` epoch milliseconds.

## Shops and menus

| Method | Path | Purpose / payload |
| --- | --- | --- |
| POST | `api/localshops` | Create with `LocalShopRequest` |
| GET | `api/localshops/nearby` | `latitude`, `longitude`, `radius`, `isMovingCart=false` |
| GET | `api/localshops/{shopId}` | Retrieve `LocalShopResponse` |
| PUT | `api/localshops/{shopId}/report` | Reason, optional note and photo URLs |
| PUT | `api/localshops/{shopId}/suggest-delete` | Suggest removal |
| POST | `api/localshops/{shopId}/verify` | Vote and optional current coordinates |
| PUT | `api/localshops/{shopId}/deactivate` | Owner deactivation |
| GET | `api/menu-items/shop/{shopId}` | Retrieve detailed menu |
| POST | `api/menu-items/manual/shop/{shopId}` | Add `items: List<MenuItemRequest>` |
| POST | `api/menu-items/image/extract` | Multipart `images`; return extracted draft items |
| POST | `api/shop-claims` | Submit `ShopClaimRequest` |
| GET | `api/shop-claims/mine` | Retrieve current user's claim outcomes |

Shop models support fixed and moving providers. `CartDetails` includes cart type, goods sold, stops, days, payments, identification hints, seasonal status, and category options. Menu items support name, food type, optional price/unit/category, availability, and optional season months.

Claims use a numeric shop ID, image URLs, coordinates, and optional owner phone. Outcomes include `APPROVED`, `PENDING_REVIEW`, and `REJECTED`.

## Community feasts

| Method | Path | Purpose / payload |
| --- | --- | --- |
| GET | `api/feasts/nearby` | Nearby active/upcoming feasts: `lat`, `lon`, `radius` |
| POST | `api/feasts` | Create with `FeastRequest` |
| GET | `api/feasts/{feastId}` | Retrieve `FeastResponse` |
| GET | `api/feasts/mine` | Organizer's own feasts |
| PUT | `api/feasts/{feastId}` | Update with `FeastRequest` |
| POST | `api/feasts/{feastId}/end` | End early |
| DELETE | `api/feasts/{feastId}` | Organizer takedown |
| PUT | `api/feasts/{feastId}/report` | Reason, optional note and photo URLs |
| PUT | `api/feasts/{feastId}/resubmit` | Correct and resubmit an eligible taken-down feast |

`FeastRequest` includes organizer/contact details, menu, food type, description, photo URLs, date, start/end times, coordinates, address/landmark, and estimated capacity. Dates use `YYYY-MM-DD`; times use `HH:mm:ss`.

Personal-list responses represent upcoming, past, and taken-down states, early ending, takedown reason, resubmission status, and resubmission eligibility.

## Reviews and report follow-up

| Method | Path | Purpose / payload |
| --- | --- | --- |
| GET | `api/reviews/shop/{shopId}` | Shop reviews |
| GET | `api/reviews/mine` | Current user's reviews |
| POST | `api/reviews` | Create with `ReviewRequest` |
| PUT | `api/reviews/{reviewId}` | Edit with `ReviewRequest` |
| DELETE | `api/reviews/{reviewId}` | Delete own review |
| PUT | `api/reviews/{reviewId}/report` | Report with optional `ReportDetails` |
| PUT | `api/reviews/{reviewId}/helpful` | Mark helpful |
| DELETE | `api/reviews/{reviewId}/helpful` | Remove helpful vote |
| GET | `api/reports/mine` | Personal reports and outcomes |
| POST | `api/reports/{kind}/{reportId}/info` | Add note/photo evidence |
| DELETE | `api/reports/{kind}/{reportId}` | Withdraw eligible report |
| POST | `api/support/bugs` | Submit `bug` description |
| POST | `api/support/features` | Submit `feature` description |

Reviews carry ratings, comments, photo URLs, helpful counts, author flags, and visibility status. Report kinds include `SHOP`, `REVIEW`, and `FEAST`; report outcomes include `IN_REVIEW`, `FIXED`, and `NO_ACTION`.

## Real-time cart messages

Connect through `/ws/websocket` using STOMP, supplying a Firebase bearer token in connection headers when available. Android vendors send positions to `/app/cart/location` and offline signals to `/app/cart/offline`. Customers subscribe to `/topic/cart/{shopId}`.

The shared `CartLocationUpdate` model carries numeric `shopId`, `ownerUid`, `lat`, `lng`, and `speed` (default `0.0`). Transport messages and publishing rights must be validated by the server.

## Error contract

`ApiClient` returns `ApiResult.Success<T>` or `ApiResult.Failure`. Failures include a kind, user-facing message, optional HTTP status, and optional field errors.

| Status / condition | Client interpretation |
| --- | --- |
| 400 | Validation failure; field errors are preferred when supplied |
| 401 / 403 | Authentication or authorization failure |
| 404 | Resource no longer available |
| 409 | Conflict, including duplicate or ineligible operations |
| 429 | Rate limit |
| 503 / other 5xx | Service unavailable / server failure |
| Network or timeout | Connectivity or timeout failure |
| Unreadable successful response | Unexpected response |

Server ownership checks, rate limits, and moderation thresholds are documented in [authentication and moderation](backend/security.md). Storage and schema details are in the [backend data model](backend/data-model.md).
