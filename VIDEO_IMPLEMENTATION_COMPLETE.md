# Video Background Implementation - Complete Setup Guide

## Overview
Your entry page has been updated to support video backgrounds that will play on repeat across iOS, Android, and Web platforms. The implementation uses platform-specific video players optimized for each platform.

## What Was Done

### 1. Core Components Created
- **EntryPage.kt** - Updated to display the video background behind the buttons
- **VideoBackground.kt** - Common expect declaration (multiplatform interface)
- **VideoBackground.android.kt** - Android implementation using ExoPlayer (Media3)
- **VideoBackground.ios.kt** - iOS implementation with AVPlayer support (ready for integration)
- **VideoBackground.js.kt** - Web/JS implementation with HTML5 video support
- **VideoBackground.wasmJs.kt** - WebAssembly implementation
- **VideoBackground.jvm.kt** - JVM desktop implementation

### 2. Dependencies Added
The following Media3 libraries were added to support Android video playback:
```
media3-exoplayer = "1.2.0"
media3-ui = "1.2.0"
```

## Video File Placement Instructions

### 📱 Android
**Location**: `app/androidApp/src/main/res/raw/entry_background.mp4`

**Steps:**
1. Navigate to `app/androidApp/src/main`
2. Create a `res` folder if it doesn't exist
3. Create a `raw` folder inside `res`
4. Place your `entry_background.mp4` video file in the `raw` folder

**How it works:** 
- Android app uses ExoPlayer (part of Media3)
- Video is loaded from the app's raw resources
- Automatically repeats using `REPEAT_MODE_ALL`
- No playback controls shown to user

### 🍎 iOS
**Location**: `app/iosApp/iosApp/entry_background.mp4`

**Steps:**
1. Open `app/iosApp/iosApp.xcodeproj` in Xcode
2. Drag and drop your `entry_background.mp4` file into the project
3. Ensure "Copy items if needed" is checked
4. Verify the file is added to the iosApp target

**How it works:**
- iOS app uses AVPlayer from AVFoundation
- Video is loaded from the app bundle
- Automatically loops on repeat
- No playback controls shown to user

### 🌐 Web
**Location**: `app/webApp/src/webMain/resources/entry_background.mp4`

**Steps:**
1. Navigate to `app/webApp/src/webMain`
2. Create a `resources` folder if it doesn't exist
3. Place your `entry_background.mp4` file in the `resources` folder

**How it works:**
- Web version uses HTML5 video element
- Can be integrated with browser's native video playback
- Automatically loops on repeat

## Video Requirements & Recommendations

### Minimum Requirements
- **Duration**: 10-15 seconds (looping video)
- **Format**: MP4 (H.264 codec)
- **Container**: MP4 file format
- **Filename**: Must be exactly `entry_background.mp4`

### Recommended Specifications
- **Resolution**: 1080p (1920x1080) for phones, 4K (3840x2160) for tablets
- **Bitrate**: 5000-8000 kbps (balance between quality and file size)
- **Frame Rate**: 30 fps
- **Audio**: Optional (video can be muted)
- **File Size**: Under 50MB total

### Prepare Your Video with FFmpeg
If you have a video that needs conversion:

```bash
# Basic conversion to MP4
ffmpeg -i input_video.mov -c:v libx264 -crf 23 -preset medium entry_background.mp4

# Optimized for mobile (downscaled)
ffmpeg -i input_video.mov \
  -c:v libx264 \
  -crf 23 \
  -preset medium \
  -s 1920x1080 \
  -r 30 \
  entry_background.mp4

# With audio muted
ffmpeg -i input_video.mov \
  -c:v libx264 \
  -crf 23 \
  -preset medium \
  -an \
  entry_background.mp4
```

## Implementation Details

### Android (ExoPlayer)
- Uses `androidx.media3:media3-exoplayer` and `androidx.media3:media3-ui`
- Loads video from `android.resource://` URI
- Handles player lifecycle with `DisposableEffect`
- No UI controls shown (`useController = false`)
- Video automatically repeats

### iOS (AVPlayer)
- Uses native `AVFoundation` framework
- Loads from app bundle main resources
- Supports automatic looping
- Player is embedded in `AVPlayerViewController`
- No playback controls shown to user

### Web (HTML5)
- Uses standard HTML5 `<video>` element
- Can be customized in index.html
- Auto-loop and auto-play supported
- Cross-browser compatible

## File Structure
```
app/
├── shared/
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/com/example/hire/
│       │       ├── EntryPage.kt (updated)
│       │       └── VideoBackground.kt (common interface)
│       ├── androidMain/
│       │   └── kotlin/com/example/hire/
│       │       └── VideoBackground.android.kt (ExoPlayer)
│       ├── iosMain/
│       │   └── kotlin/com/example/hire/
│       │       └── VideoBackground.ios.kt (AVPlayer)
│       ├── jsMain/
│       │   └── kotlin/com/example/hire/
│       │       └── VideoBackground.js.kt (HTML5)
│       ├── wasmJsMain/
│       │   └── kotlin/com/example/hire/
│       │       └── VideoBackground.wasmJs.kt (WASM)
│       └── jvmMain/
│           └── kotlin/com/example/hire/
│               └── VideoBackground.jvm.kt (Desktop)
├── androidApp/
│   └── src/main/res/raw/
│       └── entry_background.mp4 ← Place Android video here
├── iosApp/iosApp/
│   └── entry_background.mp4 ← Place iOS video here
└── webApp/
    └── src/webMain/resources/
        └── entry_background.mp4 ← Place Web video here
```

## Testing

### Android
1. Build and run: `./gradlew :app:androidApp:assembleDebug`
2. Launch the app on emulator or device
3. Verify the video plays and loops on the entry page

### iOS
1. Open `app/iosApp/iosApp.xcodeproj` in Xcode
2. Ensure `entry_background.mp4` is in the project and added to target
3. Run on simulator or device
4. Verify the video plays and loops on the entry page

### Web
1. Run: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
2. Open in browser
3. Verify the video plays and loops on the entry page

## Troubleshooting

### Video not playing on Android
- ✓ Verify file exists: `app/androidApp/src/main/res/raw/entry_background.mp4`
- ✓ Filename must be exactly `entry_background.mp4` (lowercase)
- ✓ Ensure Media3 libraries are imported in build.gradle.kts
- ✓ Check Android version is 26 or higher (minSdk = 26)

### Video not playing on iOS
- ✓ Verify file is added to Xcode project
- ✓ Check "Target Membership" in Xcode File Inspector
- ✓ Filename must be exactly `entry_background.mp4`
- ✓ Ensure file path is correct in bundle resources

### Video not playing on Web
- ✓ Verify file exists: `app/webApp/src/webMain/resources/entry_background.mp4`
- ✓ Check browser console for CORS errors
- ✓ Ensure video format is supported by browser

### Black screen instead of video
- ✓ Video file may not exist or path is incorrect
- ✓ Currently shows black background as fallback
- ✓ Check if file is in the correct location

## Next Steps

1. **Prepare your video file** - Use the FFmpeg commands above if needed
2. **Upload to Android** - Place in `app/androidApp/src/main/res/raw/`
3. **Upload to iOS** - Add to Xcode project via drag-and-drop
4. **Upload to Web** - Place in `app/webApp/src/webMain/resources/`
5. **Build and test** - Run on each platform to verify

## Performance Notes

- Videos are loaded on-demand when entry page is displayed
- Resource cleanup happens automatically when page is dismissed
- Android video is streamed (not loaded entirely into memory)
- Consider video bitrate based on target devices
- Web video may require additional optimization for mobile browsers

## Support for Adding Click Handlers

The buttons already have callback handlers ready:
```kotlin
EntryPage(
    onSignUpClick = {
        // Add your navigation logic here
    },
    onBrowseClick = {
        // Add your navigation logic here
    }
)
```

Edit `App.kt` to implement navigation when buttons are clicked.

