# Backend Development and Testing

## Code organization

Add domain changes under the corresponding `modules/<feature>` package, normally split into controller, DTO, model, repository, and service. Shared validation/security/configuration lives under `common`. Use DTO validation for external input and enforce ownership and state transitions in services.

For time-dependent behavior, use the injected `Clock` rather than the host's default time zone. Preserve post-commit boundaries for notifications and search synchronization. If adding another candidate source or scoring signal, implement the existing interfaces and test failure/timeout behavior as well as normal results.

## Test commands

Run in the backend repository:

```bash
# Unit tests through Surefire
./mvnw test

# Unit tests plus *IT.java through Failsafe; requires Docker
./mvnw verify
```

`pom.xml` binds Failsafe's integration-test and verify goals. Tests were inspected, not rerun during this documentation change.

## Integration test infrastructure

`AbstractIntegrationTest` starts the application on a random port under the `test` profile and provides MockMvc, JDBC, Redis, a controllable clock, and fake push delivery. `TestcontainersConfig` supplies real PostgreSQL/PostGIS, Redis, and Meilisearch containers.

External calls are replaced: Firebase token verification and Gemini assessment/extraction are mocked; `FakePushSender` records notifications. Before each test, the base class truncates application tables, flushes Redis, clears the search index, resets the clock and push records, and configures fixed test users.

Use only isolated test infrastructure with these fixtures: their reset logic deletes data. New integration classes should extend the base fixture and use the `*IT.java` naming convention.

Search updates are asynchronous. The `eventually(...)` helper retries assertions for up to ten seconds; use it for eventual effects instead of assuming they finish inside the request transaction. Time-sensitive tests can set `TestClock` to exercise opening hours, cart cutoff, feast expiry, and quiet hours.

## Existing coverage areas

The source includes tests for feed sourcing, scoring/selection and coordination; shop zones; cart ownership/location events and one-time follows; reviews and price detection; feast state and notifications; saved places; and common error responses. Test classes and assertions are the authority for exact coverage; this page does not assert a coverage percentage.

## Docker runtime notes

Testcontainers must reach the Docker daemon. For a Colima installation on macOS, configure the daemon socket for that installation rather than assuming Docker Desktop's path. The Failsafe configuration sets `TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock` and `TESTCONTAINERS_HOST_OVERRIDE=127.0.0.1`; these are test-process settings, not the host Docker socket location.

If container startup fails, check daemon availability, image access, resource limits, and published ports before changing application code. Do not point the test fixture at a shared developer database or search index.

## Checks for integration changes

| Change | Relevant checks |
| --- | --- |
| Controller/DTO | Validation, missing/malformed fields, status codes, and serialization |
| Ownership/security | Caller identity substitution, other-user access, unauthenticated vs Firebase anonymous identity, and admin claims |
| Feed/search | Search failure, fallback, timeout, visibility changes, stale entries, and result limits |
| Live carts | Publishing owner, inactive/rejected cart, heartbeat expiry, offline removal, and topic delivery |
| Reports/claims | Duplicate submissions, state eligibility, evidence, counters, and competing claims |
| Notifications | Transaction rollback, preferences, quiet hours, payload routes, and delivery failure |
| Account deletion | Database removal, retained content, Redis/media boundaries, and Firebase failure after commit |

The backend CI test workflow runs Maven verify. The image-publishing workflow is independent and skips tests; see [operations](operations.md) before changing delivery behavior.
