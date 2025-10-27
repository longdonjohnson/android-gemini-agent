# Build Instructions for Android Gemini Agent APK

## Option 1: Build with Android Studio (Recommended)

### Prerequisites
- Computer with Windows, macOS, or Linux
- [Android Studio](https://developer.android.com/studio) installed
- At least 8GB RAM
- 10GB free disk space

### Steps

1. **Transfer Project**
   ```bash
   # From Termux, compress the project
   cd ~
   tar -czf android-gemini-agent.tar.gz android-gemini-agent/
   
   # Transfer to your computer via:
   # - termux-setup-storage and copy to shared storage
   # - Use a cloud service (Google Drive, Dropbox)
   # - Use SSH/FTP
   ```

2. **Extract on Computer**
   ```bash
   tar -xzf android-gemini-agent.tar.gz
   cd android-gemini-agent
   ```

3. **Configure API Key**
   - Copy `local.properties.example` to `local.properties`
   - Edit and add your Gemini API key:
     ```
     GEMINI_API_KEY=your_actual_api_key_here
     sdk.dir=/path/to/Android/Sdk
     ```
   - Get API key from: https://aistudio.google.com/apikey

4. **Open in Android Studio**
   - Launch Android Studio
   - File → Open
   - Select the `android-gemini-agent` folder
   - Wait for Gradle sync to complete (may take 5-10 minutes first time)

5. **Build APK**
   - Menu: Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or run in terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

6. **Install on Android Device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   Or transfer APK to phone and install manually

## Option 2: Build with Command-Line Tools

### Prerequisites
- Android SDK Command-line Tools
- Java JDK 17
- Gradle 8.0+

### Steps

1. **Install Android SDK**
   ```bash
   # Download from: https://developer.android.com/studio#command-tools
   # Extract to /opt/android-sdk or ~/Android/Sdk
   ```

2. **Install Build Tools**
   ```bash
   sdkmanager "build-tools;34.0.0" "platforms;android-34"
   ```

3. **Set Environment Variables**
   ```bash
   export ANDROID_HOME=/path/to/android-sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```

4. **Configure local.properties**
   ```
   GEMINI_API_KEY=your_api_key_here
   sdk.dir=/path/to/android-sdk
   ```

5. **Build**
   ```bash
   cd android-gemini-agent
   chmod +x gradlew
   ./gradlew assembleDebug
   ```

6. **Install**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Option 3: Using Online Build Services

### GitHub Actions (Free)

1. Push project to GitHub
2. Create `.github/workflows/build.yml`:
   ```yaml
   name: Android Build
   on: [push]
   jobs:
     build:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - uses: actions/setup-java@v3
           with:
             java-version: '17'
             distribution: 'temurin'
         - name: Build APK
           run: |
             echo "GEMINI_API_KEY=${{ secrets.GEMINI_API_KEY }}" > local.properties
             chmod +x gradlew
             ./gradlew assembleDebug
         - uses: actions/upload-artifact@v3
           with:
             name: app-debug
             path: app/build/outputs/apk/debug/app-debug.apk
   ```
3. Add GEMINI_API_KEY to GitHub Secrets
4. Download APK from Actions artifacts

## Troubleshooting

### "SDK location not found"
- Ensure `local.properties` has correct `sdk.dir` path
- Path should point to Android SDK folder

### "Unsupported class file major version"
- Use Java 17, not newer or older versions
- Check with: `java -version`

### "Failed to resolve dependencies"
- Ensure internet connection
- Try: `./gradlew clean build --refresh-dependencies`

### "Gradle sync failed"
- Check `build.gradle` files for syntax errors
- Ensure Gradle version compatibility
- Try: File → Invalidate Caches / Restart (in Android Studio)

## After Building

### Installing the APK

**Method 1: ADB**
```bash
adb install app-debug.apk
```

**Method 2: Manual Install**
1. Transfer APK to phone
2. Open file manager
3. Tap APK file
4. Enable "Install from Unknown Sources" if prompted
5. Tap Install

### First Launch Setup

1. Open "Gemini Agent" app
2. Tap "Enable Accessibility Service"
3. Find "Gemini Agent" in list
4. Toggle ON
5. Accept permissions
6. Return to app
7. Enter a task and tap "Start Agent"

## Security Note

- Never commit `local.properties` with your API key to git
- Add to `.gitignore`:
  ```
  local.properties
  ```

## Support

For issues:
- Check the README.md
- Review logs in the app
- Ensure API key is valid
- Verify accessibility service is enabled
