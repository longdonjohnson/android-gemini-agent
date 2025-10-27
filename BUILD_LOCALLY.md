# Building APK Locally in Termux

Yes, you can build the APK directly on your Android device using Termux!

## Prerequisites Installed ✅

- ✅ Java 21 (OpenJDK)
- ✅ Gradle 9.0.0
- ✅ Android SDK with Platform 34
- ✅ Build Tools 34.0.0
- ✅ aapt2

## Quick Build

```bash
cd ~/android-gemini-agent
./build-apk.sh
```

This script will:
1. Check dependencies
2. Run the Gradle build
3. Create app-debug.apk

## Manual Build

```bash
cd ~/android-gemini-agent

# Set your API key first (optional for build, required for runtime)
nano local.properties
# Replace placeholder_key with your actual key

# Build
gradle assembleDebug

# APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

## Build Times

- **First build:** 10-20 minutes (downloads all dependencies)
- **Subsequent builds:** 2-5 minutes (dependencies cached)

## Troubleshooting

### Out of Memory
If build fails with OOM:
```bash
# Increase Gradle memory
echo "org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m" >> gradle.properties
```

### Slow Build
- Close other apps to free RAM
- Use WiFi for dependency downloads
- Be patient on first build

### Build Fails
Check the logs:
```bash
tail -100 build.log
```

Common fixes:
```bash
# Clean build
gradle clean

# Retry
gradle assembleDebug --stacktrace
```

## After Building

Your APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

Transfer to your device:
1. Copy APK to Downloads folder
2. Open file manager
3. Tap APK to install
4. Enable "Unknown Sources" if prompted

Or install via ADB:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Why Build Locally?

✅ No need for GitHub
✅ No need for computer
✅ Build directly on your device
✅ Full control over build process
✅ Test immediately

## Alternative: GitHub Actions

If local building is too slow or fails, use GitHub Actions instead (see QUICK_GITHUB.md).

---

**Note:** First build downloads ~200-300MB of dependencies. Ensure you have:
- 2GB+ free storage
- Stable internet connection  
- Patience! ☕
