# Sign-Up Feature - File Reference

## Quick File Location Guide

### Frontend UI Components

**Registration Page UI**
- **File**: `app/shared/src/commonMain/kotlin/com/example/hire/RegistrationPage.kt`
- **What**: Compose UI with form fields (Full Name, Email, Username, Password)
- **Lines**: ~200
- **Platform**: Works on iOS, Android, Web
- **Features**: Form display, error messages, success messages, loading state

**Registration View Model**
- **File**: `app/shared/src/commonMain/kotlin/com/example/hire/RegistrationViewModel.kt`
- **What**: Manages form state and validation
- **Lines**: ~150
- **Handles**: Input updates, validation, API calls, error/success messages
- **Scope**: ViewModel (lifecycle-aware)

### API & Communication

**API Service**
- **File**: `app/shared/src/commonMain/kotlin/com/example/hire/AuthApiService.kt`
- **What**: HTTP client for communicating with server
- **Lines**: ~40
- **Endpoint**: POST http://localhost:8080/api/auth/register
- **Serialization**: JSON with kotlinx.serialization

### Navigation

**Screen Navigation**
- **File**: `app/shared/src/commonMain/kotlin/com/example/hire/Navigation.kt`
- **What**: Defines screens and navigation types
- **Lines**: ~10
- **Screens**: ENTRY, REGISTRATION, BROWSE

**App Navigation Logic**
- **File**: `app/shared/src/commonMain/kotlin/com/example/hire/App.kt`
- **What**: Main app composition with navigation logic
- **Modified**: Yes, adds screen switching
- **Logic**: Handles navigation between Entry and Registration pages

### Shared Data Models

**Auth Models**
- **File**: `core/src/commonMain/kotlin/com/example/hire/AuthModels.kt`
- **What**: Data classes for registration request/response
- **Lines**: ~20
- **Models**:
  - `RegisterRequest` - Input data
  - `RegisterResponse` - Response data
- **Serialization**: @Serializable annotation

### Server Implementation

**User Repository**
- **File**: `server/src/main/kotlin/com/example/hire/UserRepository.kt`
- **What**: In-memory user storage
- **Lines**: ~60
- **Features**: 
  - Register new user
  - Check email duplicates
  - Check username duplicates
  - Retrieve users
- **Storage**: Mutable map (in-memory)

**Server Endpoint**
- **File**: `server/src/main/kotlin/com/example/hire/Application.kt`
- **What**: Ktor server with registration endpoint
- **Modified**: Yes, adds POST /api/auth/register
- **Port**: 8080
- **Features**:
  - JSON content negotiation
  - Input validation
  - Error handling
  - Response serialization

### Build Configuration

**Shared App Build**
- **File**: `app/shared/build.gradle.kts`
- **Modified**: Yes
- **Added**: Ktor client dependencies, serialization plugins

**Core Build**
- **File**: `core/build.gradle.kts`
- **Modified**: Yes
- **Added**: Serialization plugin, serialization dependencies

**Server Build**
- **File**: `server/build.gradle.kts`
- **Modified**: Yes
- **Added**: Serialization plugin, server content negotiation

**Library Versions**
- **File**: `gradle/libs.versions.toml`
- **Modified**: Yes
- **Added**: Ktor client version, Kotlin serialization version

---

## File Organization

```
app/shared/src/commonMain/kotlin/com/example/hire/
├── RegistrationPage.kt ........................... UI component
├── RegistrationViewModel.kt ..................... State management
├── AuthApiService.kt ........................... HTTP client
├── Navigation.kt ............................... Screen navigation
└── App.kt (modified) ........................... Main app composition

core/src/commonMain/kotlin/com/example/hire/
└── AuthModels.kt .............................. Shared data models

server/src/main/kotlin/com/example/hire/
├── UserRepository.kt .......................... Data storage
└── Application.kt (modified) .................. Server endpoints

gradle/
├── libs.versions.toml ......................... Library versions
├── build.gradle.kts (shared) .................. Shared app build
├── build.gradle.kts (core) ................... Core module build
└── build.gradle.kts (server) ................. Server module build
```

---

## Key Classes and Functions

### RegistrationPage.kt

```kotlin
@Composable
fun RegistrationPage(
    viewModel: RegistrationViewModel,
    onBackClick: () -> Unit,
    onSuccessClick: () -> Unit
)
```
Displays the registration form with all UI elements.

### RegistrationViewModel.kt

```kotlin
class RegistrationViewModel : ViewModel {
    val fullName: State<String>
    val email: State<String>
    val username: State<String>
    val password: State<String>
    val isLoading: State<Boolean>
    val errorMessage: State<String?>
    val successMessage: State<String?>
    
    fun updateFullName(value: String)
    fun updateEmail(value: String)
    fun updateUsername(value: String)
    fun updatePassword(value: String)
    fun register() // Main registration function
}
```

### AuthApiService.kt

```kotlin
class AuthApiService(baseUrl: String)

suspend fun register(
    fullName: String,
    email: String,
    username: String,
    password: String
): Result<RegisterResponse>
```

### UserRepository.kt

```kotlin
object UserRepository {
    fun registerUser(request: RegisterRequest): Result<User>
    fun getUserByUsername(username: String): User?
    fun getUserByEmail(email: String): User?
    fun getUserById(id: String): User?
    fun getAllUsers(): List<User>
}
```

### Application.kt

```kotlin
fun Application.module()
    POST /api/auth/register - Registration endpoint
    GET /api/users - Get all users
    GET / - Health check
```

---

## Data Flow

### Registration Flow

```
User Input
    ↓
RegistrationPage (UI)
    ↓
RegistrationViewModel (State)
    ↓ (validation)
AuthApiService (HTTP)
    ↓
POST /api/auth/register
    ↓
Application.register() (Server)
    ↓
UserRepository.registerUser()
    ↓
Response
    ↓
UI Update (success/error)
```

### Error Handling

```
Error occurs
    ↓
API Service catches it
    ↓
ViewModel gets error
    ↓
Sets errorMessage state
    ↓
UI displays red error box
```

---

## Modification Summary

### Modified Files

| File | Changes | Lines Changed |
|------|---------|---|
| `app/shared/build.gradle.kts` | Added Ktor client, serialization | +20 |
| `core/build.gradle.kts` | Added serialization support | +10 |
| `server/build.gradle.kts` | Added JSON support | +15 |
| `gradle/libs.versions.toml` | Added library versions | +10 |
| `app/shared/src/.../App.kt` | Added navigation logic | +40 |
| `server/src/.../Application.kt` | Added registration endpoint | +90 |

### Created Files

| File | Purpose | Lines |
|------|---------|-------|
| `RegistrationPage.kt` | UI Component | ~200 |
| `RegistrationViewModel.kt` | State Management | ~150 |
| `AuthApiService.kt` | HTTP Client | ~40 |
| `Navigation.kt` | Navigation Enums | ~10 |
| `AuthModels.kt` | Data Models | ~20 |
| `UserRepository.kt` | User Storage | ~60 |

---

## Configuration Reference

### Server Configuration
- **Port**: 8080
- **Host**: 0.0.0.0
- **Engine**: Netty
- **Main Class**: com.example.hire.ApplicationKt

### Client Configuration
- **Base URL**: http://localhost:8080
- **Timeout**: Default (30 seconds)
- **Serialization**: Json with lenient parsing

### Validation Configuration
- **Email**: Must contain "@"
- **Username**: Non-empty
- **Password**: Minimum 6 characters
- **Full Name**: Non-empty

---

## Testing Files

### Documentation
- `SIGNUP_IMPLEMENTATION.md` - Complete guide
- `SIGNUP_TEST_GUIDE.md` - Testing instructions
- This file - File reference

### How to Use
1. Read `SIGNUP_IMPLEMENTATION.md` for architecture
2. Follow `SIGNUP_TEST_GUIDE.md` for testing
3. Use this file for quick reference

---

## Dependencies Reference

### Frontend Dependencies
```
ktor-client-core:3.4.3
ktor-client-content-negotiation:3.4.3
ktor-client-android:3.4.3
ktor-client-ios:3.4.3
ktor-client-js:3.4.3
ktor-serialization-kotlinx-json:3.4.3
kotlinx-serialization-json:1.6.2
material-icons-extended:1.11.0
```

### Server Dependencies
```
ktor-server-core:3.4.3
ktor-server-netty:3.4.3
ktor-server-content-negotiation:3.4.3
ktor-serialization-kotlinx-json:3.4.3
kotlinx-serialization-json:1.6.2
```

---

## Common Operations

### Change Server Port
Edit `Application.kt`:
```kotlin
embeddedServer(Netty, port = 9090, ...) // Change 8080 to 9090
```

### Change Client Base URL
Edit `AuthApiService.kt`:
```kotlin
private val baseUrl: String = "https://api.example.com"
```

### Add New Field to Registration
1. Add to `RegisterRequest` in `AuthModels.kt`
2. Add state to `RegistrationViewModel`
3. Add UI field to `RegistrationPage`
4. Add validation in `RegistrationViewModel.register()`
5. Update server validation in `Application.kt`

---

## Troubleshooting Reference

### Can't compile?
→ Make sure all build.gradle.kts files are updated

### Server won't start?
→ Check if port 8080 is available

### API calls fail?
→ Make sure server is running on http://localhost:8080

### Form validation error?
→ Check error message in red box on screen

### Fields aren't updating?
→ Check that ViewModel functions are being called

---

**Last Updated**: May 25, 2026
**Completeness**: 100%
**Status**: Ready for Development

