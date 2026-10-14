# Authentication, Authorization, and Moderation

## REST identity

`FirebaseAuthFilter` reads `Authorization: Bearer <Firebase ID token>`, verifies it through Firebase Admin, and stores the UID as the Spring principal. It retains the decoded `FirebaseToken` as credentials so controllers can inspect verified claims. An invalid token leaves the request unauthenticated; protected routes are then handled by Spring Security.

Firebase anonymous sign-in is an authenticated Firebase identity. It is different from Spring's unauthenticated `anonymousUser` principal. Do not infer that every authenticated request represents a Google account.

`SecurityConfig` also enables HTTP Basic with a configured `API_USER`. That is a separate authentication mechanism and does not supply Firebase claims. Some endpoints require actual `FirebaseToken` credentials, and owner/user checks expect Firebase UIDs. Basic authentication is not a substitute for a mobile token.

## Route and ownership rules

| Area | Enforcement |
| --- | --- |
| User registration | Authenticated request; supplied Firebase UID must match the principal |
| User location | Controller overwrites request UID with authenticated UID |
| User lookup | Only the matching account can read the returned user record |
| Shop creation/update | Controller binds ownership to caller; services check stored ownership for mutation |
| Menu mutation | Service requires the shop owner; read endpoint is public |
| Feast mutation | Service requires the organizer and an eligible feast state |
| Review mutation | Service requires the author; helpful votes cannot target one's own review |
| Saved places/reports | Operations use the current principal's records |
| Shop claims | Submission requires decoded Firebase token credentials; verified email is passed only when verified |
| Administration | Controllers use `AdminAccess`, which requires a verified Firebase email in `app.admin-emails` |

Public read routes include discovery, nearby feasts/shops/carts, menus, and public reviews. Personal lists and mutation routes have additional requirements. Some controllers, including crowd pings and one-time follows, perform authentication checks locally rather than relying solely on explicit matchers. Follow-once guards currently do not exclude `anonymousUser` explicitly; review that distinction when modifying access rules.

## STOMP security

`/ws` is registered with SockJS; native clients connect through `/ws/websocket`. CONNECT can carry the same bearer token in STOMP headers. Missing or invalid tokens are accepted for watching.

The inbound interceptor rejects SEND frames outside `/app/`, preventing direct publication to `/topic` and `/queue`. `/app/cart/location` and `/app/cart/offline` require an authenticated principal. `CartLocationService` separately checks the stored cart owner, verifies the listing is a moving cart, and rejects publishing for inactive/rejected carts. Supplied `ownerUid` is replaced by the stored owner.

## Request validation and rate limits

DTOs use Jakarta validation and shared latitude/longitude/radius annotations. `@SearchRadius` permits **1–10,000 m**. Crowd heatmap additionally clamps the effective radius to **5,000 m**. Menu extraction permits up to ten image parts; the Docker profile sets 10 MB per file and 30 MB per request.

`@RateLimit` uses a Redis Lua token bucket with atomic consume/refill. The key combines the authenticated name or client IP with the literal request URI. A refusal returns 429, JSON explaining the limit, and `Retry-After` based on the configured refill period.

| Operation | Burst capacity | Refill |
| --- | --- | --- |
| Create feast | 5 | 1 token / 3,600 seconds |
| Extract menu images | 3 | 1 token / 120 seconds |
| Crowd ping | 5 | 1 token / 30 seconds |
| Crowd heatmap | 10 | 1 token / 5 seconds |
| Shop crowd lookup | 15 | 1 token / 3 seconds |
| Submit bug report | 5 | 1 token / 60 seconds |
| Submit feature suggestion | 5 | 1 token / 60 seconds |

Rate limiting applies only to annotated handlers. Redis failures are not silently converted into unlimited access. Client-IP resolution reads forwarding headers; the deployment proxy must control which forwarding headers are trusted.

## Community verification

Ordinary owner-created shops begin `PROVISIONAL`; administrator-created shops are `VERIFIED` and remain unclaimed. Votes are unique by shop/user, and creators/owners cannot verify their own listing. `NOT_SURE` records a response without changing confidence.

For YES/NO, the voter must be within **300 m** of a fixed shop or **150 m** of a cart's live position. Prompt sourcing uses 300 m for fixed shops and 100 m for live carts; prompt range and vote range are different rules.

Confidence is the sum of YES voters' trust scores minus **2.5 times** NO voters' trust scores. Missing trust defaults to **0.2**. At **1.5 or above**, a shop becomes `VERIFIED`; at **−1.0 or below**, it becomes `REJECTED` and inactive. Otherwise it stays provisional. YES voter trust is adjusted on final outcomes. These thresholds are implementation constants, not remotely configurable policies.

## Ownership claims

Claims require an active, unclaimed shop, no duplicate pending claim from the same claimant, and fewer than three claims from that user in the preceding day. Claim image URLs must use HTTPS on `firebasestorage.googleapis.com`; image fetches are size-checked, and up to two permitted listing photos are included for comparison.

For fixed shops, distance over **1,000 m** is rejected automatically. Approval can occur when distance is at most **100 m**, shopfront and name checks succeed, the listing comparison is not explicitly false, confidence is at least **0.8**, and no other open claim exists. A negative shopfront assessment with valid confidence at most **0.3** can reject a claim. Other outcomes, including AI assessment failures, remain for administrator review.

Moving carts have no fixed-distance evidence and therefore do not meet the automatic approval path. Approval transfers ownership, marks the shop verified, rejects competing pending claims, and emits shop/notification events.

## Reviews, reports, and feast state

Reviews containing price mentions are held for administrator checking. Public queries exclude pending or removed reviews as appropriate, while personal lists include their status. Review statistics are refreshed when publication or removal changes. The inspected service does **not** enforce a prohibition on owners reviewing their own shop; that check is commented out.

Shop flags and review reports go to administrator endpoints for hide/dismiss decisions. Feast reporting has a separate automatic threshold: **five** reports can take an active feast down. Organizers can correct eligible removed feasts and submit them for checking; the feast remains off the map while pending. Administrators can approve or reject resubmissions.

Reporters can add notes/photos or withdraw eligible in-review reports. `MyReportsService` exposes `IN_REVIEW`, `FIXED`, and `NO_ACTION` outcomes across target kinds. Keep report counters and notes consistent when changing withdrawal or account-deletion logic.

## Configuration boundaries

CORS is currently hardcoded to `http://localhost:4200` in the MVC configuration, while the property-based security CORS setup is commented out. WebSocket origins allow all patterns. CSRF is disabled and HTTP Basic remains enabled. Document the effective configuration rather than assuming a property declaration changes runtime behavior.

See [operations](operations.md) for credential handling and [data model](data-model.md) for deletion boundaries. This page explains implementation; it does not certify the deployment's security configuration.
