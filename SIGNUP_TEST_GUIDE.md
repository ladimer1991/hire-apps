# Quick Test Guide for Sign-Up Feature

## Prerequisites
- Kotlin 2.3.21+
- Gradle 8.x+
- Android SDK for Android testing
- Xcode for iOS testing
- Node.js for Web testing

## Step 1: Start the Server

```bash
cd /Users/main/Documents/Hire
./gradlew :server:run
```

Expected output:
```
> Task :server:run
[main] INFO io.ktor.server.netty.NettyApplicationEngine - Responding at http://0.0.0.0:8080
[main] INFO io.ktor.server.netty.NettyApplicationEngine - Automatic reload is disabled because the application was not started with --watch
```

Server is now running on `http://localhost:8080`

## Step 2: Test Server Directly (Optional)

In a new terminal, test the server:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "username": "johndoe",
    "password": "password123"
  }'
```

Expected response:
```json
{
  "success": true,
  "message": "User registered successfully",
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

## Step 3: Test on Android

```bash
# Build
./gradlew :app:androidApp:assembleDebug

# Deploy (with emulator running)
./gradlew :app:androidApp:installDebug
```

Or use Android Studio to run the app.

### Android Test Steps:
1. Launch the app
2. Click "Sign Up" button
3. Enter test data:
   - Full Name: `John Doe`
   - Email: `john@example.com`
   - Username: `johndoe`
   - Password: `password123`
4. Click "Register"
5. Verify success message appears
6. Verify screen returns to Entry page

## Step 4: Test on iOS

```bash
# Open in Xcode
open app/iosApp/iosApp.xcodeproj
```

Then:
1. Select target "iosApp"
2. Click Run (or press Cmd+R)
3. Follow same test steps as Android

## Step 5: Test on Web

```bash
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

Browser will open at `http://localhost:8080/...`

### Web Test Steps:
1. Entry page displays in browser
2. Click "Sign Up" button
3. Registration form appears
4. Fill in test data
5. Click "Register"
6. Verify success message
7. Verify navigation back to entry page

## Test Cases

### Valid Registration
**Input:**
- Full Name: `Jane Smith`
- Email: `jane@example.com`
- Username: `janesmith`
- Password: `password456`

**Expected:**
✅ Success message: "User registered successfully"
✅ User ID returned
✅ Navigation to Entry page

### Empty Fields

**Test:** Leave Full Name empty, try to register
**Expected:** ❌ Error: "Full name cannot be empty"

**Test:** Leave Email empty, try to register
**Expected:** ❌ Error: "Email cannot be empty"

**Test:** Leave Username empty, try to register
**Expected:** ❌ Error: "Username cannot be empty"

**Test:** Leave Password empty, try to register
**Expected:** ❌ Error: "Password cannot be empty"

### Invalid Email Format

**Input:** Email = `invalidemail` (no @)
**Expected:** ❌ Error: "Invalid email format"

### Short Password

**Input:** Password = `123` (less than 6 characters)
**Expected:** ❌ Error: "Password must be at least 6 characters"

### Duplicate Email

**Input:** 
1. Register with email `duplicate@example.com`
2. Try to register again with same email
**Expected:** ❌ Error: "Email already registered"

### Duplicate Username

**Input:**
1. Register with username `duplicateuser`
2. Try to register again with same username
**Expected:** ❌ Error: "Username already taken"

## Debugging

### Check Server Logs
Watch the terminal where you ran `./gradlew :server:run`

### Check Network Requests
- **Android**: Use Android Studio Network Profiler
- **iOS**: Use Xcode Network profiler or Charles Proxy
- **Web**: Use browser DevTools (F12) → Network tab

### Check Response
Add logging to `AuthApiService.kt`:
```kotlin
.onFailure { error ->
    println("Registration error: ${error.message}")
    error.printStackTrace()
}
```

## Common Issues

### "Connection refused" Error
**Solution:** Ensure server is running on `http://localhost:8080`

### "Email already registered" on first try
**Solution:** Server keeps data in memory. Restart server with:
```bash
# Kill the current server (Ctrl+C)
# Then restart
./gradlew :server:run
```

### Form shows loading state but nothing happens
**Solution:** Check network tab in browser/IDE for failed requests

### App crashes on registration
**Solution:** Check logcat (Android), console (iOS), or browser console (Web)

## Verifying Success

### Check Server Memory
View all registered users:
```bash
curl http://localhost:8080/api/users
```

Response:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "email": "john@example.com",
    "username": "johndoe",
    "password": "password123",
    "createdAt": 1716643200000
  }
]
```

### Check Form State
- Full Name field clears after success
- Email field clears after success
- Username field clears after success
- Password field clears after success
- Success message displays for 2-3 seconds
- Navigation returns to Entry page

## Next Steps After Testing

1. ✅ Test registration flow end-to-end
2. ✅ Verify validation works
3. ✅ Check error handling
4. ✅ Confirm navigation works
5. Build "Browse" page
6. Add login functionality
7. Implement persistent database
8. Add password hashing
9. Add email verification
10. Add JWT authentication

---

**Last Updated**: May 25, 2026
**Status**: Ready for Testing

