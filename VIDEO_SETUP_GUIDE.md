# Video Background Setup Guide

This guide explains where to place your video file for the entry page background across iOS, Android, and Web platforms.

## Video Requirements
- **Duration**: 10-15 seconds
- **Format**: MP4 (H.264 codec recommended)
- **File Size**: Under 50MB recommended (for app size)
- **Filename**: `entry_background.mp4`

## Directory Structure & File Placement

### For Android:
Place the video file in the Android raw resources directory:
```
app/
  androidApp/
    src/
      main/
        res/
          raw/
            entry_background.mp4  ← Add your video here
```

**Steps:**
1. Open the `app/androidApp/src/main` directory
2. Create a `res` folder (if it doesn't exist)
3. Inside `res`, create a `raw` folder (if it doesn't exist)
4. Place your `entry_background.mp4` file in the `raw` folder

### For iOS:
Place the video file in the Xcode project bundle resources:
```
app/
  iosApp/
    iosApp/
      entry_background.mp4  ← Add your video here
      Assets.xcassets/
      ContentView.swift
      Info.plist
      iOSApp.swift
```

**Steps:**
1. Open `/app/iosApp/iosApp.xcodeproj` in Xcode
2. Drag and drop your `entry_background.mp4` file into the project
3. Make sure "Copy items if needed" is checked
4. Ensure the file is added to the correct target (iosApp)

### For Web:
Place the video file in the web app resources:
```
app/
  webApp/
    src/
      webMain/
        resources/
          entry_background.mp4  ← Add your video here
```

**Steps:**
1. Navigate to `app/webApp/src/webMain`
2. Create a `resources` folder (if it doesn't exist)
3. Place your `entry_background.mp4` file in the `resources` folder

## Video File Format Recommendations

### For Best Quality and Performance:
- **Codec**: H.264 (MP4)
- **Resolution**: 1080p (1920x1080) or lower
- **Frame Rate**: 30 fps
- **Bitrate**: 5000-8000 kbps
- **Audio**: Muted or no audio track (optional)

### FFmpeg Command to Prepare Your Video:
```bash
# Convert and optimize video for mobile
ffmpeg -i input_video.mov \
  -c:v libx264 \
  -crf 23 \
  -preset medium \
  -s 1080x1920 \
  -r 30 \
  -c:a aac \
  -b:a 128k \
  entry_background.mp4
```

## How It Works

1. **Android (ExoPlayer)**: 
   - Reads the video from `android.resource://` URI
   - Automatically loops using `REPEAT_MODE_ALL`
   - No controller UI shown to users

2. **iOS (AVPlayer)**:
   - Reads from the app bundle main resources
   - Automatically loops on repeat
   - Playback controls hidden

3. **Web (HTML5)**:
   - Can be loaded from the resources folder
   - Uses HTML5 video element for playback

## Troubleshooting

### Video not playing on Android:
- Verify file is in `app/androidApp/src/main/res/raw/`
- Ensure filename is exactly `entry_background.mp4`
- Check that the package name matches your app package

### Video not playing on iOS:
- Verify file is added to Xcode project
- Check "Target Membership" in Xcode File Inspector
- Ensure filename is exactly `entry_background.mp4`

### Video not playing on Web:
- Verify file is in `app/webApp/src/webMain/resources/`
- Check browser console for errors
- Ensure CORS is configured correctly

## Testing the Video

1. Build and run the app on your device/emulator
2. The entry page should display with the video playing in the background
3. Video should automatically loop when it reaches the end
4. Verify the video scales to fill the screen

## Performance Notes

- Videos are loaded lazily when the entry page is displayed
- Resource cleanup happens automatically when the page is dismissed
- For production, consider using lower bitrate videos to reduce app size

