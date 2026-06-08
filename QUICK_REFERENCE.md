# 📋 Quick Reference Card - Video Background Setup

## 🎯 The Three Locations

| Platform | Path | Action |
|----------|------|--------|
| **Android** | `app/androidApp/src/main/res/raw/entry_background.mp4` | Copy file |
| **iOS** | `app/iosApp/iosApp/entry_background.mp4` | Copy + Drag into Xcode |
| **Web** | `app/webApp/src/webMain/resources/entry_background.mp4` | Copy file |

## 🎬 Video Format
- **Filename**: `entry_background.mp4` (exact)
- **Format**: MP4 (H.264)
- **Duration**: 10-15 seconds
- **Resolution**: 1080p recommended
- **Bitrate**: 5000-8000 kbps

## 🚀 One-Liner Setup

```bash
# Android
mkdir -p app/androidApp/src/main/res/raw && cp video.mp4 app/androidApp/src/main/res/raw/entry_background.mp4

# iOS
cp video.mp4 app/iosApp/iosApp/entry_background.mp4

# Web
mkdir -p app/webApp/src/webMain/resources && cp video.mp4 app/webApp/src/webMain/resources/entry_background.mp4
```

## ✅ Verification

```bash
ls app/androidApp/src/main/res/raw/entry_background.mp4
ls app/iosApp/iosApp/entry_background.mp4
ls app/webApp/src/webMain/resources/entry_background.mp4
```

## 🎯 What's Implemented

✅ Entry page with video background
✅ Two buttons (Sign Up / Browse)
✅ Android video playback (ExoPlayer)
✅ iOS video playback (AVPlayer)
✅ Web video playback (HTML5)
✅ Auto-looping on all platforms
✅ No playback controls shown
✅ Media3 dependencies added

## 📚 Documentation

- `VIDEO_FILE_PLACEMENT.md` - Detailed paths with visuals
- `VIDEO_IMPLEMENTATION_COMPLETE.md` - Technical deep-dive
- `QUICK_START_VIDEO.md` - Quick reference
- `IMPLEMENTATION_STATUS.md` - Status overview
- `FINAL_SUMMARY.md` - Complete summary

## 🔄 What Happens Next

1. Add your MP4 file to the 3 locations above
2. Rebuild the app
3. Video appears on entry page
4. Video loops continuously
5. Buttons work normally

## 💡 Pro Tips

- Use 1080p video to balance quality and file size
- Keep video duration to 10-15 seconds for smooth looping
- Test on actual devices for best results
- Lower bitrate (5000 kbps) for smaller app size
- Test video on all platforms before release

## ❓ Common Questions

**Q: Can I use a different filename?**
A: No, must be exactly `entry_background.mp4`

**Q: What if video doesn't exist?**
A: App shows black background (graceful fallback)

**Q: Can I add sound to the video?**
A: Yes, but will play through device speaker

**Q: How do I test?**
A: Run on Android emulator, iOS simulator, and in browser

**Q: Can I update the video later?**
A: Yes, just replace the file and rebuild

---

**Status**: ✅ Ready for video upload
**Time to completion**: ~5 minutes (place files + rebuild)
**Platforms supported**: iOS ✅ | Android ✅ | Web ✅

