# Local Feast · Bhandara

Local Feast helps people discover nearby food shops, moving vendors, and community feasts. The Bhandara repository contains its Android application, shared Kotlin Multiplatform product code, iOS host, and documentation.

The product combines everyday food discovery with time-sensitive community information: where a cart is now, when a feast is happening, and what a local shop serves.

## Explore the documentation

- [Product overview](docs/index.md): purpose, audiences, and product experience.
- [Investor brief](docs/investor-brief.md): product thesis, potential business models, and validation priorities.
- [Feature catalogue](docs/features.md): implemented capabilities and platform differences.
- [Roadmap and readiness](docs/roadmap.md): known gaps and proposed next milestones.
- [Architecture](docs/architecture.md): shared code and native integrations.
- [Getting started](docs/getting-started.md): Android and iOS setup.
- [Development](docs/development.md): code organization, checks, and documentation workflow.
- [Backend integration](docs/backend-integration.md) and [API reference](docs/api-reference.md): mobile integration contract.
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

Documentation reviewed against the mobile source on **14 October 2026**. Implemented client functionality does not by itself establish public release status, backend availability, or commercial traction.
