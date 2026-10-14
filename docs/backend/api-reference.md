# Backend API Reference

This reference covers REST controller mappings in backend revision `f3751bf`, reviewed on **14 October 2026**. Paths begin at the backend base URL. The [mobile API reference](../api-reference.md) covers only routes wrapped by shared clients; this page also includes administrator and server-only operations.

## Conventions

Use `Authorization: Bearer <Firebase ID token>` for authenticated requests. Firebase anonymous users have valid Firebase identities; account sign-in gates in the mobile UI are a separate constraint. Ownership and administrator checks apply beyond authentication. Read [authentication and moderation](security.md) before adding a client.

Use JSON for ordinary payloads and multipart form data with `images` parts for menu extraction. Empty successful responses are used by several mutations, including account deletion, token/preference updates, report withdrawal, and item/review deletion. Do not assume every 2xx response contains JSON.

| Query | Coordinates | Default radius | Response |
| --- | --- | --- | --- |
| `GET /api/feed` | `lat`, `lon` | 5,000 m | `activeShops`, `verificationPrompt`, `zones` |
| `GET /api/feasts/nearby` | `lat`, `lon` | 500 m | Feast list |
| `GET /api/localshops/nearby` | `latitude`, `longitude` | 5,000 m | Shop list; optional `isMovingCart` |
| `GET /api/carts/nearby` | `lat`, `lng` | 500 m | Active cart list |
| `GET /api/crowd/heatmap` | `lat`, `lng` | 2,000 m; effective cap 5,000 m | Aggregated cells |

Validated search radii are 1–10,000 m before endpoint-specific rules. Feed `cuisines` is optional context; the inspected ranking signals do not establish a cuisine filter. The mobile search field filters returned listings locally.

## Route inventory

Each table links the Java controller and names its handler, providing a direct starting point for request/response DTOs and status codes. These routes are extracted from annotations, including endpoints omitted from older README examples. Handler names are source identifiers, not public protocol fields.

### Carts

[CartController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/cart/controller/CartController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/carts/nearby` | `getNearbyActiveCarts()` |

[OneTimeFollowController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/cart/controller/OneTimeFollowController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/carts/{cartId}/follow-once` | `followOnce()` |
| DELETE | `/api/carts/{cartId}/follow-once` | `unfollowOnce()` |
| GET | `/api/carts/{cartId}/follow-once/status` | `isFollowingOnce()` |

### Crowd

[CrowdController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/crowd/controller/CrowdController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/crowd/ping` | `ping()` |
| GET | `/api/crowd/heatmap` | `getHeatmap()` |
| GET | `/api/crowd/shop/{shopId}` | `getShopCrowdLevel()` |

### Feasts

[FeastController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/feast/controller/FeastController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/feasts` | `createFeast()` |
| GET | `/api/feasts/nearby` | `getFeastsNearby()` |
| GET | `/api/feasts/mine` | `getMyFeasts()` |
| PUT | `/api/feasts/{id}` | `updateFeast()` |
| PUT | `/api/feasts/{id}/resubmit` | `resubmitFeast()` |
| POST | `/api/feasts/{id}/end` | `endFeastEarly()` |
| DELETE | `/api/feasts/{id}` | `takeDownFeast()` |
| GET | `/api/feasts/{id}` | `getFeastById()` |
| PUT | `/api/feasts/{id}/report` | `reportFeast()` |

### Feed

[FeedController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/feed/controller/FeedController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/feed` | `getFeed()` |

### Localshops

[LocalShopController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/localshops/controller/LocalShopController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/localshops` | `createLocalShop()` |
| GET | `/api/localshops/{id}` | `getLocalShopById()` |
| PUT | `/api/localshops/{id}` | `updateLocalShop()` |
| PUT | `/api/localshops/location` | `updateLocalShopLocation()` |
| PUT | `/api/localshops/{id}/report` | `reportLocalShop()` |
| PUT | `/api/localshops/{id}/deactivate` | `deactivateLocalShop()` |
| GET | `/api/localshops/nearby` | `findNearbyShops()` |
| PUT | `/api/localshops/{id}/suggest-delete` | `suggestDeleteLocalShop()` |
| POST | `/api/localshops/{id}/verify` | `verifyLocalShop()` |

### Menu Items

[MenuItemController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/menuitem/controller/MenuItemController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/menu-items/manual/shop/{shopId}` | `saveManualMenuItems()` |
| POST | `/api/menu-items/image/extract` | `extractMenuItemsFromImage()` |
| GET | `/api/menu-items/shop/{shopId}` | `getMenu()` |
| PUT | `/api/menu-items/{id}` | `updateMenuItem()` |
| DELETE | `/api/menu-items/{id}` | `deleteMenuItem()` |

### Reports

[MyReportsController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/myreports/controller/MyReportsController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/reports/mine` | `getMyReports()` |
| POST | `/api/reports/{kind}/{reportId}/info` | `addInfo()` |
| DELETE | `/api/reports/{kind}/{reportId}` | `withdraw()` |

### Reviews

[ReviewController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/review/controller/ReviewController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/reviews` | `createReview()` |
| PUT | `/api/reviews/{id}` | `updateReview()` |
| DELETE | `/api/reviews/{id}` | `deleteReview()` |
| GET | `/api/reviews/mine` | `getMyReviews()` |
| GET | `/api/reviews/{id}` | `getReviewById()` |
| PUT | `/api/reviews/{id}/helpful` | `markHelpful()` |
| DELETE | `/api/reviews/{id}/helpful` | `unmarkHelpful()` |
| PUT | `/api/reviews/{id}/report` | `reportReview()` |
| GET | `/api/reviews/shop/{shopId}` | `getReviewsByShopId()` |

### Saved

[SavedPlacesController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/saved/controller/SavedPlacesController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/saved` | `getSaved()` |
| POST | `/api/saved/sync` | `sync()` |

### Shop Claims

[ShopClaimController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/shopclaim/controller/ShopClaimController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/shop-claims` | `submitClaim()` |
| GET | `/api/shop-claims/mine` | `getMyClaims()` |

### Support

[BugController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/support/bug/controller/BugController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/support/bugs` | `createBug()` |

[FeatureController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/support/feature/controller/FeatureController.java)

| Method | Path | Handler |
| --- | --- | --- |
| POST | `/api/support/features` | `createFeature()` |

### Users

[UserController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/user/controller/UserController.java)

| Method | Path | Handler |
| --- | --- | --- |
| DELETE | `/api/users/me` | `deleteMyAccount()` |
| POST | `/api/users` | `createUser()` |
| PUT | `/api/users/location` | `updateUserLocation()` |
| PUT | `/api/users/push-token` | `updatePushToken()` |
| PUT | `/api/users/notifications` | `updateNotifications()` |
| GET | `/api/users/notifications` | `getNotifications()` |
| GET | `/api/users/{id}` | `getUserById()` |

### Legal Pages

[LegalPagesController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/common/legal/LegalPagesController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/privacy` | `privacyPolicy()` |
| GET | `/privacy/` | `privacyPolicy()` |

### Administration

[AdminFeastController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/feast/controller/AdminFeastController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/admin/feasts/resubmitted` | `listResubmitted()` |
| POST | `/api/admin/feasts/{id}/approve` | `approve()` |
| POST | `/api/admin/feasts/{id}/reject` | `reject()` |

[AdminReportsController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/myreports/controller/AdminReportsController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/admin/reports/{kind}/{targetId}` | `reportsAbout()` |

[AdminReviewController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/review/controller/AdminReviewController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/admin/reviews/reported` | `listReported()` |
| POST | `/api/admin/reviews/{id}/hide` | `hide()` |
| POST | `/api/admin/reviews/{id}/dismiss-reports` | `dismissReports()` |
| GET | `/api/admin/reviews/awaiting-check` | `listAwaitingCheck()` |
| POST | `/api/admin/reviews/{id}/approve` | `approve()` |
| POST | `/api/admin/reviews/{id}/reject` | `reject()` |

[AdminShopClaimController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/shopclaim/controller/AdminShopClaimController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/admin/shop-claims` | `listClaims()` |
| POST | `/api/admin/shop-claims/{id}/approve` | `approve()` |
| POST | `/api/admin/shop-claims/{id}/reject` | `reject()` |

[AdminShopController](https://github.com/piyushchouhan/be-bhandara/blob/f3751bf/src/main/java/com/localfeast/backend/modules/localshops/controller/AdminShopController.java)

| Method | Path | Handler |
| --- | --- | --- |
| GET | `/api/admin/shops/flagged` | `listFlagged()` |
| POST | `/api/admin/shops/{id}/hide` | `hide()` |
| POST | `/api/admin/shops/{id}/dismiss-flags` | `dismissFlags()` |

## Request and response families

| Operation | Contract details |
| --- | --- |
| User registration | `UserRequestDTO`: Firebase UID, FCM token, latitude, longitude. UID must match the caller. |
| Location update | `UserUpdateLocationDTO`: caller identity overrides the submitted UID; location and token update the user's stored record. |
| Notification preferences | `UserNotificationsDTO`: `nearbyFeasts`, `reportUpdates`, `shopReviews`, `claimStatus`, `quietHours`. Omitted fields retain existing values. |
| Shop creation/update | `LocalShopRequestDTO`: name/type, owner contact, menu names, images, coordinates/address, hours, amenities, optional moving-cart details and external ordering URLs. Owner UID is bound to the caller. Update is a full DTO operation, not JSON Merge Patch. |
| Detailed menu | `MenuItemBulkRequestDTO` contains `items`. Each item includes name, food type, price, availability, optional unit/category and season months. Duplicate names conflict. |
| Menu extraction | Up to ten image parts under `images`; response contains draft `items`. Extraction does not save menu records. |
| Ownership claim | `ShopClaimRequestDTO`: numeric `shopId`, image URLs, latitude/longitude, optional owner phone. Response includes decision state, evidence/assessment metadata, and reason. |
| Feast create/update | `FeastRequestDTO`: organizer/contact details, menu, food type, description, image URLs, `feastDate`, start/end times, coordinates/address, and capacity. Caller controls organizer identity. |
| Review create/update | Create includes target shop, rating, comment, image URLs; update targets an existing review. Price mentions can make it pending rather than publicly visible. |
| Report evidence | Notes and photo URLs accompany reports or follow-up information. Report kinds are `SHOP`, `REVIEW`, and `FEAST`. |
| Saved-place sync | `saved` and `removed` lists contain `kind`, string `id`, optional epoch-millisecond `savedAt`. Response is the account's full authoritative list. |
| Support | Bug submission uses `bug`; feature suggestion uses `feature`. |

Server DTOs under each module define field validation, enum values, and nullable fields. Dates use ISO local dates; feast times use local times. IDs may be strings in response DTOs even when controller path parameters are `Long`.

## Example reads

```bash
curl --get "$API_BASE_URL/api/feed" \
  --data-urlencode 'lat=19.0760' \
  --data-urlencode 'lon=72.8777' \
  --data-urlencode 'radius=5000'

curl "$API_BASE_URL/api/reports/mine" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"
```

Set `API_BASE_URL` to the server origin without a trailing slash in these examples. Obtain the ID token through the client authentication flow; never substitute a service-account key.

## Saved-place example

```json
{
  "saved": [{"kind": "SHOP", "id": "42"}],
  "removed": [{"kind": "FEAST", "id": "17"}]
}
```

Submit this shape to `POST /api/saved/sync`. Nonexistent targets are skipped and the service caps an account at 500 entries. This is delta synchronization, not replacement of the whole account list.

## Real-time protocol

Register through SockJS `/ws`; native STOMP clients use `/ws/websocket`. Set the bearer token in STOMP CONNECT headers when publishing. An ordinary HTTP GET to the path is not a STOMP session.

| Destination | Operation | Handler / result |
| --- | --- | --- |
| `/app/cart/location` | SEND | Validate principal/owner, update Redis presence, broadcast position |
| `/app/cart/offline` | SEND | Validate owner, remove live presence |
| `/topic/cart/{shopId}` | SUBSCRIBE | Receive accepted position DTOs |

```json
{
  "shopId": 42,
  "ownerUid": "caller-uid",
  "lat": 19.0760,
  "lng": 72.8777,
  "speed": 1.25
}
```

The cart owner is established from the authenticated principal, not from `ownerUid`. Customer speed display interprets speed as meters per second. Presence expires after 120 seconds without a refresh. The broker permits watching without authentication; publishing requires a valid owner identity.

## Error responses

`GlobalExceptionHandler` normally emits `message`, `statusCode`, and optional `errors` keyed by field/parameter. Controllers that catch exceptions or return empty status responses can have different bodies; security-filter failures are also outside the controller advice. Parse bodies defensively.

```json
{
  "message": "Validation failed",
  "statusCode": 400,
  "errors": {"radius": "Radius must be between 1 and 10000 meters"}
}
```

| Status | Typical meaning |
| --- | --- |
| 400 | Invalid JSON, field/query validation, or invalid operation arguments |
| 401 / 403 | Missing identity or unauthorized caller/role; exact status can depend on the security path |
| 404 | Missing resource or route |
| 409 | Duplicate/conflicting record or ineligible lifecycle transition |
| 429 | Annotated token bucket exhausted; inspect `Retry-After` |
| 503 | AI service unavailable or unusable response |
| 500 | Unexpected server failure |

The project declares Springdoc/OpenAPI support. Verify `/v3/api-docs` against the running build when producing client schemas; this static inventory does not assert runtime Swagger availability or complete generated schemas.
