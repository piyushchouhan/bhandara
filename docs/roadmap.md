# Implementation Gaps

This page records incomplete interfaces and integration boundaries identified in the source review on **14 October 2026**. It is a developer reference, not a delivery schedule.

## Mobile implementation

| Area | Current behavior | Development boundary |
| --- | --- | --- |
| Owner operations | Timing and manual open/closed controls show “Coming soon” | The backend supports full shop updates, including opening/closing times; the mobile editor is incomplete. A dedicated manual open/closed override is not established by that API. |
| Shop lifecycle | Owners can deactivate a shop | Permanent deletion and reactivation controls are incomplete |
| Menu management | Mobile supports adding detailed menu items | The backend also has item update/delete endpoints that are not exposed through the inspected shared `MenuApi` |
| Profile | Google account details are displayed | In-app profile name/photo editing remains a placeholder |
| Owner statistics | Weekly statistics are displayed without a connected analytics implementation | Do not use these controls as a source of measured activity |
| iOS native services | Shared discovery and cart viewing work through native bridges | Vendor publishing, push delivery, and periodic user-location synchronization lack Android equivalents |
| Offline behavior | Listing drafts and saved-place changes persist locally | Discovery still requires backend access |
| One-time cart alerts | The server has follow-once endpoints and a `cart_proximity` payload | The inspected mobile clients do not wrap these endpoints or route this notification type |

## Backend integration and operations

| Area | Detail |
| --- | --- |
| API defaults | Feed defaults to 5 km; nearby feast and cart controllers default to 500 m. Mobile clients may pass a different radius explicitly. |
| Time rules | The feed hides moving candidates at or after 21:00 in the configured app time zone; the nearby-cart endpoint is a separate service path. |
| Event durability | Search synchronization and notifications use in-process events. There is no durable event outbox or active Kafka transport. |
| WebSocket distribution | Spring's simple broker is local to one instance. Shared Redis state does not distribute subscriptions across instances. |
| CORS | The MVC configuration hardcodes a localhost origin; changing `CORS_ALLOWED_ORIGINS` alone does not activate the commented security configuration. |
| Migrations | Profiles use Hibernate schema update. A SQL file exists under `db/migration`, but the POM does not configure Flyway or Liquibase. |
| Local Compose | The older Compose file omits Redis/Meilisearch services and does not supply the `DB_*` variables required by the Docker profile. It is not a complete one-command development stack. |
| Release gating | The backend test workflow and image-publishing workflow are separate; image publishing skips tests and does not depend on the test job. |
| Credentials | Some profile files contain literal credential values. Use environment-backed configuration and review those files before distribution; values are intentionally omitted from this site. |

## Data cleanup boundaries

`AccountDeletionService` removes database account records and related personal contributions, reassigns retained shops/feasts to `deleted-user`, clears specified contact/audit fields, and attempts Firebase identity deletion after commit. It does not clear every descriptive field: for example, `Feast.organizerName` is not explicitly cleared. Firebase identity deletion can fail after database deletion has committed.

Firebase Storage objects and Redis follow registrations are not explicitly removed by that service. Follow-once registrations have no explicit TTL in `OneTimeFollowService`. Document and test the required cleanup behavior before treating account deletion as complete across every storage system.

## Validation when changing these areas

Verify the affected mobile flow against the server, including permission denial, expired credentials, ownership rejection, upload failure, network recovery, and account switching. For server rules, add focused service or integration tests; see [backend testing](backend/testing.md). For deployment changes, confirm profile resolution, credentials, schema state, and service connectivity in the intended environment.
