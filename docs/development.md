# Development Guide

## Development Environment

### Required Tools

- Android Studio (Arctic Fox or newer)
- JDK 11+
- Git
- Android SDK

## Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

### File Organization

- One class per file
- Keep files under 300 lines when possible
- Group related functionality

### Naming Conventions

- **Classes**: PascalCase (`MainActivity`, `UserViewModel`)
- **Functions**: camelCase (`onCreate()`, `getUserData()`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_COUNT`, `API_KEY`)
- **Variables**: camelCase (`userName`, `itemCount`)

## Git Workflow

### Branch Naming

- `feature/feature-name` - New features
- `bugfix/bug-description` - Bug fixes
- `hotfix/critical-issue` - Urgent fixes
- `refactor/component-name` - Code refactoring

### Commit Messages

Follow conventional commits:

```
feat: add user authentication
fix: resolve crash on startup
docs: update README
refactor: simplify data layer
```

## Building the Project

### Debug Build

```bash
./gradlew assembleDebug
```

### Release Build

```bash
./gradlew assembleRelease
```

## Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Code Review

- All changes require a pull request
- At least one approval required
- CI checks must pass

## Documentation

- Document public APIs with KDoc
- Update this documentation when adding features
- Keep README.md up to date
