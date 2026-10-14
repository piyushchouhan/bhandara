# Backend Setup and Operations

## Prerequisites

- JDK **21** and the repository's Maven wrapper.
- PostgreSQL with **PostGIS** enabled. Integration fixtures use `postgis/postgis:15-3.4`.
- Redis. The server Compose configuration uses `redis:7-alpine`.
- Meilisearch. The repository uses `getmeili/meilisearch:v1.12` in server/test configurations.
- A Firebase service account for the intended project.
- Gemini credentials when exercising extraction, claim assessment, or zone naming.

Run backend commands from the `be-bhandara` repository. This site's source and MkDocs workflow remain in `bhandara`.

## Profiles and environment

`application.yml` selects `dev` by default. Profile-specific files are `application-dev.yml`, `application-docker.yml`, `application-azure.yml`, and `application-prod.yml`. Do not copy their literal credential values into documentation or deployment scripts.

For environment-backed configuration, the Docker profile uses:

| Variable | Purpose / configured default |
| --- | --- |
| `DB_URL` | PostgreSQL JDBC URL; required |
| `DB_USERNAME`, `DB_PASSWORD` | Database credentials; required |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Redis connection; defaults to `redis:6379`, optional password |
| `MEILI_HOST` | Search URL; defaults to `http://meilisearch:7700` |
| `MEILI_API_KEY` | Search access key; required by profile configuration |
| `MEILI_INDEX_NAME` | Defaults to `local_shops` |
| `GEMINI_API_KEY` | AI credential |
| `GEMINI_MODEL` | Docker-profile default `gemini-2.5-flash`; verify availability for the selected account |
| `API_SECURITY_USERNAME`, `API_SECURITY_APIKEY` | HTTP Basic configuration; username defaults to `admin` |
| `FIREBASE_SERVICE_ACCOUNT_PATH` | Resource path; defaults to `file:/app/service-account.json` |
| `SERVER_PORT` | Overrides the Docker profile's port 80 |
| `SPRING_PROFILES_ACTIVE` | Select `docker` when using this variable mapping |

The Azure profile uses `AZURE_POSTGRESQL_CONNECTIONSTRING` for the datasource URL. Profile keys and environment names are not interchangeable: the Docker profile expects `DB_URL`.

The code also reads `app.admin-emails`, `app.time-zone`, `proximity.alert.radius-meters`, `feed.*`, `meilisearch.*`, and `zones.*` properties. Use Spring property overrides as needed. `CORS_ALLOWED_ORIGINS` appears in profile files, but the effective MVC CORS implementation is hardcoded; see [authentication and moderation](security.md).

## Run the application locally

1. Start PostgreSQL/PostGIS, Redis, and Meilisearch and create an empty application database.
2. Enable PostGIS in that database: `CREATE EXTENSION IF NOT EXISTS postgis;`.
3. Supply the variables above through a local environment manager or untracked shell configuration. For host execution, override service names with reachable hosts such as `127.0.0.1`.
4. Set `FIREBASE_SERVICE_ACCOUNT_PATH` to a readable external `file:/absolute/path/...json` resource. Keep it outside source control.
5. Run the application with the environment-backed profile and a local port:

```bash
SPRING_PROFILES_ACTIVE=docker SERVER_PORT=8080 ./mvnw spring-boot:run
```

This command assumes the remaining required variables have already been supplied. Maven and Spring Boot do not automatically load a Compose `.env` file into a host process.

The application initializes Firebase, configures the search index, and seeds active shops if that index is empty. Firebase initialization logs an error and can allow startup to continue when the service account cannot be read, so startup alone does not verify authentication is usable.

Profiles currently rely on Hibernate schema update. Review [schema initialization](data-model.md#schema-initialization) before applying the supplied index SQL; a fresh database must have tables before indexes can reference them.

## Build and container execution

```bash
./mvnw clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

The JAR command inherits the selected profile and environment. `-DskipTests` is a packaging option, not validation; run [the test suite](testing.md) separately.

`Dockerfile` uses a Java 21 build stage and Java 21 runtime stage, runs as a non-root user, defaults to the Docker profile, and uses port 80. `JAVA_OPTS` controls JVM settings; defaults include a 512 MB maximum heap. The image health check calls `/actuator/health`.

`docker-compose.server.yml` runs the application image, Redis, Meilisearch, and Watchtower with an **external** PostgreSQL database. It reads `.env`, mounts a Firebase JSON file read-only, and uses persistent Redis/search volumes. After configuring those files and GHCR access:

```bash
docker compose -f docker-compose.server.yml up -d
docker compose -f docker-compose.server.yml logs --tail=100 app
```

The older `docker-compose.yml` is incomplete for the current service: it lacks Redis/Meilisearch services, supplies an Azure datasource variable instead of required Docker `DB_*` variables, and mounts index SQL that expects tables to exist. Do not treat it as a verified development quick start.

## CI and delivery

| Workflow file | Actual behavior |
| --- | --- |
| `.github/workflows/azure-deploy.yml` | Despite its filename, it is named **Build & Test** and runs `mvn clean verify -q` on main pushes, pull requests, or manual dispatch |
| `.github/workflows/docker.yml` | Builds with tests skipped and publishes `latest` and short-SHA image tags to GHCR on main pushes/manual dispatch |

These are separate workflows; image publication is not gated by the test workflow. The server Compose contains Watchtower configured to poll for image updates every 300 seconds. Its presence documents intended update configuration, not evidence that a particular server is running it successfully.

`pom.xml` also includes an Azure deployment plugin. Before using deployment commands, confirm which delivery path is active and supply the target environment configuration explicitly.

## Health and diagnosis

Docker/Azure profiles expose Actuator `health` and `info`, with health details enabled. Confirm the intended access policy at the deployment edge. Springdoc dependencies/configuration are present for `/swagger-ui.html` and `/v3/api-docs`; runtime generation and dependency compatibility must be verified against the deployed build.

| Symptom | Inspect |
| --- | --- |
| Empty or partial feed | Source timeout logs, database contents, opening hours, 21:00 cart filter, visibility state, and requested radius |
| Shop missing from search | Post-commit listener logs, Meilisearch tasks, configured index, and source retry cooldown |
| Cart missing | Owner authentication, active/verification state, 120-second heartbeat, Redis reachability, and vendor connection |
| No proximity push | Follow registration, stored location snapshot, cart distance, FCM token, and local event processor logs |
| No feast push | 500 m recipient range, user location/token, excluded organizer, preferences, quiet hours, and send results |
| Repeated 429 | Endpoint-specific UID/IP bucket and refill period |
| AI failure | Configured model/key, network timeouts, image payload, and upstream response handling |
| Account data gone but sign-in remains | Firebase deletion error after database transaction commit |

Backup/restore processes and reverse-proxy/TLS configuration are not defined by these application files. Record them in the deployment environment's runbook rather than inferring them from the presence of container volumes.
