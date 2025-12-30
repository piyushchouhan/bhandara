# Backend Integration TODO

## User Data Sync

### Required API Endpoint
```
POST /api/users/update
```

### Request Payload
```json
{
  "uid": "firebase-anonymous-uid",
  "fcmToken": "fcm-device-token",
  "latitude": 18.5823749,
  "longitude": 73.8844745,
  "timestamp": 1704045600000
}
```

### Implementation Steps

1. **Add Retrofit dependency** to `app/build.gradle.kts`:
   ```kotlin
   implementation("com.squareup.retrofit2:retrofit:2.9.0")
   implementation("com.squareup.retrofit2:converter-gson:2.9.0")
   implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
   ```

2. **Create API service** in `data/api/`:
   ```kotlin
   interface BhandaraApiService {
       @POST("users/update")
       suspend fun updateUser(@Body request: UserUpdateRequest): Response<Unit>
   }
   ```

3. **Create request/response models** in `data/models/`:
   ```kotlin
   data class UserUpdateRequest(
       val uid: String,
       val fcmToken: String?,
       val latitude: Double?,
       val longitude: Double?,
       val timestamp: Long = System.currentTimeMillis()
   )
   ```

4. **Implement API client** in `data/api/`:
   ```kotlin
   object ApiClient {
       private const val BASE_URL = "https://your-backend.com/api/"
       
       val instance: BhandaraApiService by lazy {
           Retrofit.Builder()
               .baseUrl(BASE_URL)
               .addConverterFactory(GsonConverterFactory.create())
               .build()
               .create(BhandaraApiService::class.java)
       }
   }
   ```

5. **Update UserManager.kt**:
   - Uncomment `sendUserDataToBackend()` calls
   - Implement the function to call API
   - Handle errors and retries

### Backend Database Schema (PostgreSQL + PostGIS)

```sql
CREATE EXTENSION postgis;

CREATE TABLE users (
    uid VARCHAR(128) PRIMARY KEY,
    fcm_token VARCHAR(512) NOT NULL,
    location GEOGRAPHY(POINT, 4326),
    last_active TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT valid_location CHECK (
        ST_X(location::geometry) BETWEEN -180 AND 180 AND
        ST_Y(location::geometry) BETWEEN -90 AND 90
    )
);

CREATE INDEX idx_users_location ON users USING GIST (location);
CREATE INDEX idx_users_last_active ON users (last_active);
```

### Backend API Implementation Example (FastAPI)

```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from geoalchemy2 import Geography
from sqlalchemy import create_engine
from datetime import datetime

app = FastAPI()

class UserUpdateRequest(BaseModel):
    uid: str
    fcmToken: str | None
    latitude: float | None
    longitude: float | None
    timestamp: int

@app.post("/api/users/update")
async def update_user(request: UserUpdateRequest):
    # Upsert user data
    query = """
        INSERT INTO users (uid, fcm_token, location, last_active)
        VALUES (:uid, :fcm_token, 
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :last_active)
        ON CONFLICT (uid) 
        DO UPDATE SET
            fcm_token = COALESCE(:fcm_token, users.fcm_token),
            location = CASE 
                WHEN :lat IS NOT NULL AND :lon IS NOT NULL 
                THEN ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
                ELSE users.location
            END,
            last_active = :last_active
    """
    
    # Execute query
    return {"status": "success"}
```

### Testing

1. Start backend server
2. Update `BASE_URL` in ApiClient
3. Run app on emulator
4. Check backend logs for incoming requests
5. Verify data in PostgreSQL:
   ```sql
   SELECT uid, fcm_token, 
          ST_X(location::geometry) as longitude,
          ST_Y(location::geometry) as latitude,
          last_active
   FROM users;
   ```

### Notes

- **Error handling**: Implement retry logic for failed API calls
- **Offline support**: Queue updates locally if network unavailable
- **Privacy**: Implement data retention policies
- **Security**: Use HTTPS and API authentication tokens
