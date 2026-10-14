# Backend Architecture and Flows

## Runtime layout

```text
Mobile / API client
    -> Spring MVC + Spring Security
    -> Controllers -> Domain services
                      -> PostgreSQL/PostGIS: records and spatial queries
                      -> Redis: presence, crowd, follows, rate limits
                      -> Meilisearch: nearby candidate IDs
                      -> Firebase Admin: token verification and push
                      -> Gemini: image extraction/assessment and zone labels

Vendor STOMP client -> /ws -> /app/cart/location
    -> ownership validation -> Redis position/presence
    -> /topic/cart/{shopId} -> customer subscribers
    -> local asynchronous event -> proximity alert processor
```

The application enables asynchronous work and scheduling. The STOMP broker is Spring's in-process simple broker, not an external message broker.

## Discovery pipeline

`FeedController` builds a `FeedContext` from `lat`, `lon`, `radius`, optional `cuisines`, caller identity, and the configured clock. `FeedPipelineCoordinator` executes these stages:

1. **Source candidates:** stationary shops from Meilisearch, moving carts from Redis, and provisional verification candidates. A dedicated executor has 16 threads and a queue capacity of 200 by default.
2. **Hydrate:** load shop details from PostgreSQL. Menu and image collections are fetched separately to avoid multiple-bag fetch problems. Moving candidates use their live coordinates.
3. **Filter:** apply visibility, time rules, and verification-prompt exclusions.
4. **Score:** sum distance, rating, and verification signals, then rank.
5. **Partition:** return up to 100 active shop/cart candidates and the nearest eligible provisional prompt.
6. **Enrich:** add best-effort crowd levels for fixed shops and calculate zones from returned fixed shops.

Each source has a two-second completion limit. Failed or timed-out sources contribute no candidates; this can yield a partial feed. A completion timeout does not imply that the underlying database or HTTP work was canceled.

Stationary and provisional sourcing fall back to PostGIS queries when Meilisearch raises `SearchUnavailableException`. Search uses two-second connection and three-second read timeouts, a 1,000-candidate limit, and a 30-second retry cooldown. A first failed search can exceed the feed source deadline before fallback completes; subsequent requests during cooldown can take the fallback directly.

## Ranking and visibility

Current scoring signals are deterministic:

| Signal | Contribution |
| --- | --- |
| Distance | `5 * exp(-distanceMeters / 2000)` |
| Rating | `averageRating * (1 - exp(-totalRatings / 10))` |
| Verified status | `+1` for `VERIFIED` |

The time filter excludes moving feed candidates from 21:00 onward and excludes candidates outside configured opening/closing hours. Missing hours do not hide a shop. `app.time-zone` defaults to `Asia/Kolkata`; time-of-day rules use the injected `Clock`.

The controller accepts cuisine preferences in the context; the inspected scoring signals do not establish cuisine-based personalization. Do not treat this parameter as a guaranteed preference filter.

## Shop zones

`ShopZoneService` groups fixed shops from the selected feed. Default rules connect neighbors within 15 m, omit groups smaller than five, split groups larger than ten, and produce a PostGIS concave outline padded by 5 m. Zone identity derives from member IDs.

Stored labels include English, Hindi, and Marathi names plus a zone type. Missing or stale names are queued for Gemini in the background, so geometry can be returned without waiting for naming. Failed naming retries are delayed 30 minutes; unused labels are eligible for removal after 30 days. Zone generation errors return an empty zone list rather than failing the feed.

## Search synchronization

Shop changes publish `LocalShopChangedEvent`. `MeilisearchSyncListener` runs asynchronously after transaction commit, synchronizing publicly visible shops and removing hidden/rejected ones. Startup configures the index and seeds active shops if the index is empty.

This is eventual consistency through in-process listeners. A nonempty index is not fully rebuilt at every startup, and failed post-commit work has no durable outbox in the inspected source. Database hydration and visibility checks reduce the impact of stale index entries; they do not make index updates transactional.

## Live-cart updates

`StompAuthInterceptor` authenticates the CONNECT header. Unauthenticated connections may watch; publishing position or offline messages requires a principal. `CartLocationService` checks the stored owner's UID and moving-cart/public-visibility state, then replaces the supplied owner UID with the stored value.

Each accepted position updates Redis GEO, refreshes presence and display metadata for **120 seconds**, broadcasts the DTO, and emits a location event. Expired presence removes the cart from active lookups; explicit offline calls remove GEO and metadata immediately. Shop visibility events also evict hidden carts.

The shared Redis index does not distribute WebSocket subscriptions. Scaling beyond one application instance requires a plan for broker distribution and asynchronous work ownership.

## One-time proximity alerts

`OneTimeFollowService` records a cart/user association and copies the user's stored database location into Redis GEO. The local event processor finds followers within a configurable radius, default **500 m**, sends a `cart_proximity` push when a token exists, then removes the registration for the found user.

The location is a snapshot from registration, not a continuous tracking subscription. Follow sets have no explicit TTL. This processor calls `NotificationService` directly, so the `UserNotifier` category/quiet-hours gate is not applied on this path. The current mobile notification router does not handle `cart_proximity`.

## Notifications and scheduled work

New-feast notifications run asynchronously after commit, select registered users within **500 m**, exclude the organizer, and honor nearby-feast preferences and quiet hours. Report outcomes, published reviews, and claim decisions have separate event listeners. `UserNotifier` suppresses its categories from 22:00 to 07:00 when quiet hours are enabled. Delivery is best effort; listeners are not a durable queue.

| Job | Schedule | Purpose |
| --- | --- | --- |
| `FeastExpiryScheduler` | Every 5 minutes | Deactivate expired feasts using the application clock |
| `CrowdCleanupScheduler` | Every 10 minutes | Remove expired crowd GEO members |
| `ZoneLabelCleanupScheduler` | Sunday at 03:30 | Delete labels not seen within the configured retention window |

Cron trigger times follow the scheduler's configured/default time zone unless explicitly set. The normal main entry point sets the process zone from `APP_TIME_ZONE`, default `Asia/Kolkata`. Overriding the injected clock alone, such as in tests, does not change the scheduler's zone.

## Failure boundaries

Search-source timeouts, zone calculation, and crowd enrichment can degrade the feed without failing the whole request. PostgreSQL remains necessary for hydration. Redis is required for rate-limited operations and live features. Gemini failures map to unavailable responses for extraction, while claim assessment can fall back to administrator review. See [operations](operations.md) for diagnosis.
