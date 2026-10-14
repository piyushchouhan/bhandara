# The Android app still parses these models with Gson, which matches JSON keys to field names.
# Keep the names so release (R8-minified) builds keep reading the backend's JSON correctly.
-keep class com.example.bhandara.data.models.api.** { *; }
