# Video File Placement - Visual Directory Structure

This guide shows exactly where to place your `entry_background.mp4` file for each platform.

## 📱 ANDROID - Complete Path

```
Hire/
└── app/
    └── androidApp/
        └── src/
            └── main/
                ├── AndroidManifest.xml
                ├── kotlin/
                │   └── com/example/hire/MainActivity.kt
                └── res/                          ← CREATE if doesn't exist
                    ├── drawable/
                    ├── layout/
                    ├── mipmap/
                    ├── values/
                    └── raw/                      ← CREATE this folder
                        └── entry_background.mp4  ← PLACE YOUR VIDEO HERE ✅
```

**Quick Path**: `app/androidApp/src/main/res/raw/entry_background.mp4`

**To create:**
```bash
mkdir -p app/androidApp/src/main/res/raw
cp /path/to/your/entry_background.mp4 app/androidApp/src/main/res/raw/
```

---

## 🍎 iOS - Complete Path

```
Hire/
└── app/
    └── iosApp/
        ├── iosApp.xcodeproj/          ← Open this in Xcode
        └── iosApp/
            ├── ContentView.swift
            ├── Info.plist
            ├── iOSApp.swift
            ├── Assets.xcassets/
            ├── Preview Content/
            └── entry_background.mp4    ← PLACE YOUR VIDEO HERE ✅
                                         (Via Xcode drag-and-drop)
```

**Quick Path**: `app/iosApp/iosApp/entry_background.mp4`

**To add in Xcode:**
1. Open `app/iosApp/iosApp.xcodeproj` in Xcode
2. Right-click on the "iosApp" folder in the left sidebar
3. Select "Add Files to 'iosApp'..."
4. Select your `entry_background.mp4` file
5. Check "Copy items if needed"
6. Click "Add"

**Or via command line + Xcode:**
```bash
cp /path/to/your/entry_background.mp4 app/iosApp/iosApp/
# Then drag the file into Xcode to register it with the project
```

---

## 🌐 WEB - Complete Path

```
Hire/
└── app/
    └── webApp/
        └── src/
            ├── commonMain/
            └── webMain/
                ├── kotlin/
                │   └── com/example/hire/main.kt
                └── resources/                  ← CREATE if doesn't exist
                    └── entry_background.mp4   ← PLACE YOUR VIDEO HERE ✅
```

**Quick Path**: `app/webApp/src/webMain/resources/entry_background.mp4`

**To create:**
```bash
mkdir -p app/webApp/src/webMain/resources
cp /path/to/your/entry_background.mp4 app/webApp/src/webMain/resources/
```

---

## 📋 Verification Checklist

After placing your video files:

### Android ✓
```bash
# Check file exists
ls -la app/androidApp/src/main/res/raw/entry_background.mp4

# Expected output: -rw-r--r-- ... entry_background.mp4
```

### iOS ✓
```bash
# Check file exists
ls -la app/iosApp/iosApp/entry_background.mp4

# Expected output: -rw-r--r-- ... entry_background.mp4

# Verify in Xcode: File Inspector shows correct target membership
```

### Web ✓
```bash
# Check file exists
ls -la app/webApp/src/webMain/resources/entry_background.mp4

# Expected output: -rw-r--r-- ... entry_background.mp4
```

---

## 🎬 Video File Naming

**IMPORTANT**: The filename MUST be exactly:
```
entry_background.mp4
```

❌ Wrong:
- `EntryBackground.mp4` (wrong case)
- `entry-background.mp4` (wrong separator)
- `background.mp4` (wrong name)
- `video.mp4` (wrong name)
- `entry_background.MP4` (wrong extension case)

✅ Correct:
- `entry_background.mp4` (exactly this)

---

## 🔍 Directory Tree View

Here's the complete relevant structure:

```
Hire/
├── README.md
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml         ← Media3 dependencies added
├── app/
│   ├── shared/
│   │   ├── build.gradle.kts       ← Media3 dependencies added
│   │   └── src/
│   │       ├── commonMain/
│   │       │   └── kotlin/com/example/hire/
│   │       │       ├── EntryPage.kt               ← Updated
│   │       │       ├── VideoBackground.kt        ← NEW
│   │       │       └── App.kt                     ← Using EntryPage
│   │       ├── androidMain/
│   │       │   └── kotlin/com/example/hire/
│   │       │       └── VideoBackground.android.kt ← NEW
│   │       ├── iosMain/
│   │       │   └── kotlin/com/example/hire/
│   │       │       └── VideoBackground.ios.kt    ← NEW
│   │       ├── jsMain/
│   │       │   └── kotlin/com/example/hire/
│   │       │       └── VideoBackground.js.kt     ← NEW
│   │       ├── wasmJsMain/
│   │       │   └── kotlin/com/example/hire/
│   │       │       └── VideoBackground.wasmJs.kt ← NEW
│   │       └── jvmMain/
│   │           └── kotlin/com/example/hire/
│   │               └── VideoBackground.jvm.kt    ← NEW
│   │
│   ├── androidApp/
│   │   └── src/
│   │       └── main/
│   │           ├── AndroidManifest.xml
│   │           ├── kotlin/
│   │           └── res/
│   │               └── raw/
│   │                   └── entry_background.mp4  ← PLACE ANDROID VIDEO HERE
│   │
│   ├── iosApp/
│   │   ├── iosApp.xcodeproj
│   │   └── iosApp/
│   │       ├── ContentView.swift
│   │       ├── iOSApp.swift
│   │       ├── Info.plist
│   │       ├── Assets.xcassets/
│   │       ├── Preview Content/
│   │       └── entry_background.mp4  ← PLACE iOS VIDEO HERE
│   │
│   └── webApp/
│       └── src/
│           ├── commonMain/
│           └── webMain/
│               ├── kotlin/
│               └── resources/
│                   └── entry_background.mp4  ← PLACE WEB VIDEO HERE
│
└── Documentation files created:
    ├── VIDEO_SETUP_GUIDE.md
    ├── VIDEO_IMPLEMENTATION_COMPLETE.md
    ├── QUICK_START_VIDEO.md
    └── IMPLEMENTATION_STATUS.md
```

---

## ⚡ Quick Copy Commands

If you have your video ready, use these commands:

```bash
# Navigate to project root
cd /Users/main/Documents/Hire

# Create Android directory and copy
mkdir -p app/androidApp/src/main/res/raw
cp /path/to/entry_background.mp4 app/androidApp/src/main/res/raw/

# Copy to iOS (then add to Xcode project)
cp /path/to/entry_background.mp4 app/iosApp/iosApp/

# Create Web directory and copy
mkdir -p app/webApp/src/webMain/resources
cp /path/to/entry_background.mp4 app/webApp/src/webMain/resources/

# Verify all copies
echo "=== Android ===" && ls -la app/androidApp/src/main/res/raw/entry_background.mp4
echo "=== iOS ===" && ls -la app/iosApp/iosApp/entry_background.mp4
echo "=== Web ===" && ls -la app/webApp/src/webMain/resources/entry_background.mp4
```

---

## 🎯 Next Steps

1. **Prepare your video** - 10-15 seconds, MP4 format, 1080p recommended
2. **Create directories** - Run the mkdir commands above
3. **Copy the files** - Use the cp commands above
4. **Add to Xcode** (iOS only) - Drag file into Xcode project
5. **Rebuild** - Run `./gradlew build` or your IDE's build command
6. **Test** - Run on Android, iOS, and Web

That's it! Your video backgrounds will now appear on the entry page! 🎬

