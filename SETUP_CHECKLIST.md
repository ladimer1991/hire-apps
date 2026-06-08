# ✅ VIDEO BACKGROUND SETUP - CHECKLIST

## Pre-Setup Checklist

- [ ] I have a 10-15 second video file
- [ ] The video is in MP4 format (H.264 codec)
- [ ] The video resolution is 1080p or higher
- [ ] The video file is under 50MB
- [ ] I've renamed the file to `entry_background.mp4` (exact name)

## Android Setup

- [ ] Created folder: `app/androidApp/src/main/res/raw/`
- [ ] Copied `entry_background.mp4` to `app/androidApp/src/main/res/raw/`
- [ ] Verified file exists: `ls app/androidApp/src/main/res/raw/entry_background.mp4`
- [ ] Filename is exactly `entry_background.mp4` (lowercase, no spaces)

## iOS Setup

- [ ] Copied `entry_background.mp4` to `app/iosApp/iosApp/`
- [ ] Verified file exists: `ls app/iosApp/iosApp/entry_background.mp4`
- [ ] Opened `app/iosApp/iosApp.xcodeproj` in Xcode
- [ ] Dragged `entry_background.mp4` into Xcode project
- [ ] Checked "Copy items if needed" in the dialog
- [ ] Verified target membership is set to "iosApp"
- [ ] Filename is exactly `entry_background.mp4` (lowercase, no spaces)

## Web Setup

- [ ] Created folder: `app/webApp/src/webMain/resources/`
- [ ] Copied `entry_background.mp4` to `app/webApp/src/webMain/resources/`
- [ ] Verified file exists: `ls app/webApp/src/webMain/resources/entry_background.mp4`
- [ ] Filename is exactly `entry_background.mp4` (lowercase, no spaces)

## Code Verification

- [ ] `EntryPage.kt` exists and calls `VideoBackground()`
- [ ] `VideoBackground.kt` exists with `expect` declaration
- [ ] `VideoBackground.android.kt` exists with actual implementation
- [ ] `VideoBackground.ios.kt` exists with actual implementation
- [ ] `VideoBackground.js.kt` exists with actual implementation
- [ ] `VideoBackground.wasmJs.kt` exists with actual implementation
- [ ] `VideoBackground.jvm.kt` exists with actual implementation
- [ ] `libs.versions.toml` has media3 version
- [ ] `shared/build.gradle.kts` has media3 dependencies

## Build & Test

### Android
- [ ] Run: `./gradlew :app:androidApp:assembleDebug`
- [ ] Build succeeds without errors
- [ ] Deploy to Android emulator or device
- [ ] Entry page displays with video background
- [ ] Video plays and loops continuously
- [ ] Sign Up and Browse buttons work
- [ ] No playback controls visible

### iOS
- [ ] Open `app/iosApp/iosApp.xcodeproj` in Xcode
- [ ] Select target: iosApp
- [ ] Build succeeds without errors
- [ ] Run on iOS simulator or device
- [ ] Entry page displays with video background
- [ ] Video plays and loops continuously
- [ ] Sign Up and Browse buttons work
- [ ] No playback controls visible

### Web
- [ ] Run: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
- [ ] Build succeeds without errors
- [ ] Browser opens to http://localhost:8080
- [ ] Entry page displays with video background
- [ ] Video plays and loops continuously
- [ ] Sign Up and Browse buttons work
- [ ] No playback controls visible

## Troubleshooting

If something doesn't work:

- [ ] Verify video filename is exactly `entry_background.mp4`
- [ ] Check that video file is in the correct location for each platform
- [ ] Ensure video format is MP4 with H.264 codec
- [ ] Try with a different, simpler video file
- [ ] Clean build directories: `./gradlew clean`
- [ ] Rebuild the app
- [ ] Check IDE console for error messages

### Android Issues
- [ ] Verify `res/raw/` folder structure is correct
- [ ] Check that minSdk is 26 or higher
- [ ] Ensure Media3 dependencies are properly imported
- [ ] Try running on a different emulator/device

### iOS Issues
- [ ] Check Xcode's File Inspector for target membership
- [ ] Verify file is in the correct folder
- [ ] Try re-adding file to Xcode project
- [ ] Clean Xcode build folder: Cmd+Shift+K
- [ ] Rebuild the app

### Web Issues
- [ ] Check browser console for errors (F12)
- [ ] Verify resources folder exists
- [ ] Check that file is accessible to browser
- [ ] Try a different browser

## Final Verification

- [ ] Android video plays ✅
- [ ] iOS video plays ✅
- [ ] Web video plays ✅
- [ ] All videos loop continuously ✅
- [ ] All buttons work normally ✅
- [ ] No console errors ✅
- [ ] No crashes on any platform ✅

## Documentation

- [ ] Read `VIDEO_FILE_PLACEMENT.md` - Detailed paths
- [ ] Read `VIDEO_IMPLEMENTATION_COMPLETE.md` - Full technical docs
- [ ] Read `QUICK_REFERENCE.md` - Quick cheat sheet
- [ ] Read `README_VIDEO_SETUP.md` - Overview

## Ready for Production

- [ ] All tests passed on Android
- [ ] All tests passed on iOS
- [ ] All tests passed on Web
- [ ] Video quality is good
- [ ] App size is acceptable
- [ ] Performance is smooth
- [ ] Ready to deploy! 🚀

---

## 🎯 Quick Path to Success

1. ✅ Prepare your video (10-15 seconds, MP4)
2. ✅ Run the bash commands from `VIDEO_FILE_PLACEMENT.md`
3. ✅ Add iOS video to Xcode via drag-and-drop
4. ✅ Run `./gradlew clean build`
5. ✅ Test on all three platforms
6. ✅ You're done! 🎉

## 📞 Need Help?

Refer to:
- `VIDEO_FILE_PLACEMENT.md` - For exact paths
- `VIDEO_IMPLEMENTATION_COMPLETE.md` - For technical details
- `QUICK_REFERENCE.md` - For quick commands

---

**Last Updated**: May 22, 2026
**Status**: Implementation Complete, Awaiting Video Files
**Estimated Setup Time**: 5-10 minutes

