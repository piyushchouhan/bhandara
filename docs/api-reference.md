# API Reference

## MainActivity

Main entry point of the application.

### Class: `MainActivity`

```kotlin
class MainActivity : AppCompatActivity()
```

Main activity of the Bhandara application.

#### Methods

##### `onCreate()`

```kotlin
protected fun onCreate(savedInstanceState: Bundle?)
```

Called when the activity is starting.

**Parameters:**
- `savedInstanceState` - If the activity is being re-initialized, this contains the data it most recently supplied

---

## Components

(Document your app components here as you build them)

### ViewModels

Document your ViewModels here.

### Repositories

Document your data repositories here.

### Services

Document your services here.

### Utilities

Document utility classes and helper functions here.

---

## KDoc Generation

To generate KDoc documentation:

```bash
./gradlew dokkaHtml
```

Generated docs will be in `app/build/dokka/html/`
