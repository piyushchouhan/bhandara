# Local Feast · Bhandara

Local Feast helps people discover nearby food shops, moving vendors, and community feasts. The Bhandara repository contains its Android application, shared Kotlin Multiplatform product code, iOS host, and documentation.

The mobile apps use Kotlin Multiplatform and Compose Multiplatform. The companion `be-bhandara` service uses Java 21 and Spring Boot with PostgreSQL/PostGIS, Redis, and Meilisearch.

## Explore the documentation

- [Project overview](docs/index.md): repository layout, runtime components, and reading guide.
- [Feature catalogue](docs/features.md): implemented capabilities and platform differences.
- [Implementation gaps](docs/roadmap.md): incomplete controls and platform differences.
- [Architecture](docs/architecture.md): shared code and native integrations.
- [Getting started](docs/getting-started.md): Android and iOS setup.
- [Development](docs/development.md): code organization, checks, and documentation workflow.
- [Backend documentation](docs/backend/index.md): server architecture, data model, complete route inventory, security, operations, and testing.
- [Backend integration](docs/backend-integration.md) and [mobile API reference](docs/api-reference.md): app/server integration.
- [Firebase and platform services](docs/firebase-setup.md): authentication, storage, notifications, and permissions.

Published documentation: [Local Feast documentation](https://piyushchouhan.github.io/bhandara/). Documentation changes pushed to `main` are published by `.github/workflows/docs.yml` once GitHub Pages is configured to use GitHub Actions.

## Repository structure

```text
app/         Android host, Firebase integrations, notifications, vendor tracking
shared/      Shared Compose UI, ViewModels, models, APIs, preferences, and tests
  src/commonMain/
  src/androidMain/
  src/iosMain/
  src/commonTest/
iosApp/      SwiftUI host, native bridges, and XcodeGen configuration
docs/        MkDocs documentation source
```

## Preview the documentation

```bash
source venv/bin/activate
mkdocs serve
```

Validate the site with `mkdocs build --strict`. If the environment is missing, create a virtual environment and run `python -m pip install -r requirements-docs.txt` first. See the [development guide](docs/development.md) for the full workflow.

Documentation reviewed against the mobile source on **14 October 2026**. The documentation distinguishes mobile implementation, backend behavior, and deployment configuration.
