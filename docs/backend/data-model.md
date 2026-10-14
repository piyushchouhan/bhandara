# Backend Data Model

## Persistent records

JPA entities are grouped by feature. PostgreSQL stores account records and contributions; PostGIS provides geographic points and spatial operations. Table names below come from entity mappings.

| Table | Purpose / relationships |
| --- | --- |
| `users` | Numeric ID, unique Firebase UID, FCM token, location, notification preferences, trust score, and audit fields |
| `local_shops` | Owner UID/contact, shop/cart details, location, operating hours, amenities, rating summaries, visibility, and verification state |
| `menu_items` | Detailed shop items, food type, price/unit/category, availability, seasonal months, source, and price timestamps |
| `reviews` | Shop association, reviewer UID, rating, text/photos, publication/moderation flags, and helpful count |
| `review_helpful_votes` | One vote per review and voter |
| `review_reports` | One report per review and reporter |
| `local_shop_votes` | One verification vote per shop and voter |
| `shop_flags` | Shop reports/deletion suggestions, unique by shop, user, and flag type |
| `shop_claims` | Claimant, photo evidence, coordinates, AI assessment, decision, and administrator audit |
| `feasts` | Organizer UID/details, food/photos, date/time, geographic location, active/report/resubmission state |
| `feast_reports` | One report per feast and reporter |
| `report_notes` | Notes and photo evidence attached to reports |
| `saved_places` | User UID, kind, target ID, and saved time; unique by user, kind, and target |
| `shop_zone_labels` | Stored zone membership/input hash, localized labels, type, and last-seen information |
| `bugs`, `features` | Submitted issue reports and feature suggestions |

`reviews` is unique by shop and reviewer. Deleting a review through the ordinary endpoint makes it inactive; a later create can reuse that record. Moderator removal prevents creating a replacement review for that shop.

Some related lists are mapped as element collections and create additional collection tables. Entity mappings are authoritative for these tables and column names; this page is not a replacement SQL schema.

## Coordinates, IDs, and time

JTS point construction uses **X = longitude, Y = latitude** with SRID 4326. User location is mapped as `geography(Point,4326)`, and spatial repositories use PostGIS operations such as `ST_DWithin`. API radii and reported distances use meters.

Controller query names differ: feed/feasts use `lat` and `lon`; carts/crowd use `lat` and `lng`; nearby shops use `latitude` and `longitude`. See [API reference](api-reference.md).

Server entity IDs are generally `Long`. Mobile response models often expose these as strings; clients should follow the endpoint DTO rather than assuming one JSON type everywhere.

Feast dates and times are separate local values. Application time rules use the injected clock, defaulting to `Asia/Kolkata`. The normal main entry point also sets the process default zone from `APP_TIME_ZONE` (default `Asia/Kolkata`); some audit timestamps use that process default through `LocalDateTime.now()` directly. Tests or alternative launch paths need to account for this separately. Saved-place synchronization uses epoch milliseconds. Do not interpret every timestamp as a UTC instant.

## Redis structures

| Key / prefix | Structure / purpose | Lifetime |
| --- | --- | --- |
| `moving_carts` | GEO index of current cart positions | Members removed explicitly or during stale lookup |
| `cart:active:{id}` | Cart presence marker | 120 seconds from last position |
| `cart:meta:{id}` | Display metadata JSON | 120 seconds |
| `cart:followers:once:{cartId}` | Set of one-time follower UIDs | No explicit TTL |
| `user:follows:once:{uid}` | Set of followed cart IDs | Removed as registrations are cleared |
| `user_locations` | GEO snapshots for one-time alerts | Removed when a user has no remaining follows |
| `crowd:geo` | GEO index keyed by daily derived token | Expired members cleaned on lookup and by scheduler |
| `crowd:ping:{token}` | Rounded coordinates | 5 minutes |
| `crowd:first:{token}` | First-seen time for long-stay exclusion | 2 hours |
| `crowd:limiter:{identity}:{requestURI}` | Token-bucket hash | At least 1 hour; sized to refill duration |

Verification also uses the `shop:active_prompts:` prefix for lease bookkeeping. Review its callers before changing prompt scheduling.

Crowd tokens are SHA-256 of UID plus date, and coordinates are rounded to four decimal places. This is a pseudonymous derivative, not an assurance of irreversible anonymization. Active pings whose first-seen duration exceeds 30 minutes are excluded by the current heuristic; this is not a verified distinction between customers and employees.

Crowd levels use a 50 m radius: fewer than three active tokens is `QUIET`, three through seven is `MODERATE`, and eight or more is `BUSY`. Heatmap output includes only grid cells with at least five active tokens.

## Saved-place merge rules

`SavedPlacesService.sync` processes removals first, then additions, and returns the full account list newest first. It skips nonexistent targets, preserves the original time for an existing save, clamps future timestamps to the current instant, and caps an account at 500 entries. The response must be treated as authoritative even when requested additions exceed the cap.

## Schema initialization

The inspected profiles use `spring.jpa.hibernate.ddl-auto=update`. The repository has `db/migration/V1__create_spatial_index.sql`, but the POM has no Flyway/Liquibase dependency. Its directory name alone does not make it an automatically executed migration.

The old local Compose mounts this directory as PostgreSQL initialization SQL. Its index statements reference `feasts`, so the table must exist before those statements run. For an existing database, create PostGIS first, let the application schema exist, then apply reviewed index SQL in the appropriate environment. Introduce a versioned migration runner before depending on ordered production schema changes.

## Account deletion

`AccountDeletionService` transactionally withdraws eligible outstanding reports, deletes database reviews/votes/reports/notes/claims/saves and the user row, then retains shops/feasts under `deleted-user` with specified owner/contact/audit fields cleared. Shop changes trigger index synchronization. Firebase identity deletion is attempted after commit and logs a failure without rolling back database deletion.

The implementation does not explicitly clear `Feast.organizerName`, delete Firebase Storage objects, or remove Redis follows. Those are concrete cleanup boundaries to consider when changing retention behavior; see [implementation gaps](../roadmap.md).
