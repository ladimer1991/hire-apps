# Sign-Up Implementation Guide

## Overview

A complete sign-up system has been implemented with:
- **Frontend**: Registration form UI for iOS, Android, and Web
- **Backend**: Ktor server endpoint for user registration
- **Validation**: Client-side and server-side validation
- **Navigation**: Screen navigation from entry page to registration page

## Architecture

### Frontend Components

#### 1. **RegistrationPage.kt** (UI)
A Compose UI component that displays a registration form with:
- Full Name field
- Email field
- Username field
- Password field (masked)
- Register button
- Back button for navigation
- Error and success message displays

**Location**: `app/shared/src/commonMain/kotlin/com/example/hire/RegistrationPage.kt`

#### 2. **RegistrationViewModel.kt** (State Management)
Manages the registration form state:
- Input field states (fullName, email, username, password)
- Loading state
- Error and success messages
- Validation logic
- API call handling

**Location**: `app/shared/src/commonMain/kotlin/com/example/hire/RegistrationViewModel.kt`

#### 3. **AuthApiService.kt** (HTTP Client)
Handles API communication with the server:
- Makes HTTP POST requests to `/api/auth/register`
- Serializes/deserializes JSON
- Error handling

**Location**: `app/shared/src/commonMain/kotlin/com/example/hire/AuthApiService.kt`

#### 4. **AuthModels.kt** (Data Models)
Shared data models for registration:
```kotlin
@Serializable
data class RegisterRequest(
    val fullName: String,
    val email: String,
    val username: String,
    val password: String
)

@Serializable
data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val userId: String? = null
)
```

**Location**: `core/src/commonMain/kotlin/com/example/hire/AuthModels.kt`

#### 5. **Navigation.kt** (Screen Navigation)
Defines screen types and navigation logic:
```kotlin
enum class Screen {
    ENTRY,
    REGISTRATION,
    BROWSE
}
```

**Location**: `app/shared/src/commonMain/kotlin/com/example/hire/Navigation.kt`

### Backend Components

#### 1. **Application.kt** (Server Setup)
Ktor server configuration with:
- Content negotiation for JSON
- POST `/api/auth/register` endpoint
- Request validation
- Response serialization

**Location**: `server/src/main/kotlin/com/example/hire/Application.kt`

#### 2. **UserRepository.kt** (Data Storage)
In-memory user storage with:
- User registration
- Duplicate email/username checking
- User lookup functions

**Location**: `server/src/main/kotlin/com/example/hire/UserRepository.kt`

## Flow Diagram

```
User launches app
    ↓
Entry Page displays
    ↓
User clicks "Sign Up" button
    ↓
Navigation changes to Registration Page
    ↓
User fills in form (Full Name, Email, Username, Password)
    ↓
User clicks "Register" button
    ↓
RegistrationViewModel validates input
    ↓
AuthApiService sends POST to /api/auth/register
    ↓
Server validates and checks for duplicates
    ↓
UserRepository creates new user
    ↓
Server responds with success/error
    ↓
UI shows success/error message
    ↓
Navigation returns to Entry Page on success
```

## Validation Rules

### Frontend Validation
- Full Name: Cannot be empty
- Email: Cannot be empty, must contain "@"
- Username: Cannot be empty
- Password: Cannot be empty, minimum 6 characters

### Server Validation
- Full Name: Cannot be empty
- Email: Valid format, must not already exist
- Username: Cannot be empty, must be unique
- Password: Minimum 6 characters

## API Endpoint

### POST /api/auth/register

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "username": "johndoe",
  "password": "password123"
}
```

**Success Response (HTTP 201):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error Response (HTTP 400/500):**
```json
{
  "success": false,
  "message": "Error description"
}
```

## Platform Support

| Platform | Status | Notes |
|----------|--------|-------|
| Android | ✅ Ready | Uses Ktor Android client |
| iOS | ✅ Ready | Uses Ktor iOS client |
| Web | ✅ Ready | Uses Ktor JS client |
| Server | ✅ Ready | Ktor server on port 8080 |

## Dependencies Added

### Frontend
- `ktor-client-core` - HTTP client
- `ktor-client-content-negotiation` - JSON serialization
- `ktor-client-android` - Android HTTP engine
- `ktor-client-ios` - iOS HTTP engine
- `ktor-client-js` - Web HTTP engine
- `ktor-serialization-kotlinx-json` - JSON serialization
- `kotlinx-serialization-json` - Serialization library
- `material-icons-extended` - Icons for UI

### Server
- `ktor-server-content-negotiation` - JSON handling
- `ktor-serialization-kotlinx-json` - JSON serialization
- `kotlinx-serialization-json` - Serialization library

## How to Test

### 1. Start the Server
```bash
./gradlew :server:run
```

### 2. Run Android App
```bash
./gradlew :app:androidApp:assembleDebug
# Deploy to emulator/device
```

### 3. Run iOS App
Open `app/iosApp/iosApp.xcodeproj` in Xcode and run.

### 4. Run Web App
```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

### 5. Test Registration
1. Launch app (any platform)
2. Click "Sign Up" button
3. Fill in form with test data:
   - Full Name: Test User
   - Email: test@example.com
   - Username: testuser
   - Password: password123
4. Click "Register"
5. You should see success message

## API Testing with curl

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "username": "testuser",
    "password": "password123"
  }'
```

## Future Enhancements

1. **Database Integration**: Replace in-memory storage with real database
2. **Password Hashing**: Use bcrypt for password security
3. **Email Verification**: Send verification email before activation
4. **JWT Tokens**: Issue tokens for authenticated sessions
5. **Login Screen**: Add login page for existing users
6. **Profile Management**: Allow users to update their profile
7. **Error Recovery**: Better error messages and recovery options

## File Structure

```
app/
├── shared/src/commonMain/kotlin/com/example/hire/
│   ├── RegistrationPage.kt
│   ├── RegistrationViewModel.kt
│   ├── AuthApiService.kt
│   ├── Navigation.kt
│   └── App.kt (updated)
core/
└── src/commonMain/kotlin/com/example/hire/
    └── AuthModels.kt
server/
└── src/main/kotlin/com/example/hire/
    ├── Application.kt (updated)
    └── UserRepository.kt
```

## Configuration

### Server Port
Default: `8080`
Configure in `Application.kt`:
```kotlin
embeddedServer(Netty, port = 8080, host = "0.0.0.0", ...)
```

### Client Base URL
Default: `http://localhost:8080`
Configure in `AuthApiService.kt`:
```kotlin
private val baseUrl: String = "http://localhost:8080"
```

## Notes

- **In-Memory Storage**: Current implementation uses in-memory storage. Data is lost on server restart.
- **Password Security**: Passwords are NOT hashed. Use bcrypt in production.
- **Email Validation**: Only checks format, doesn't verify actual email address.
- **CORS**: May need to configure CORS for web platform in production.

---

**Implementation Date**: May 25, 2026
**Status**: ✅ Complete and Ready
**Platforms**: iOS ✅ | Android ✅ | Web ✅

