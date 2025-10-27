# Android Gemini Agent

On-device AI agent using Gemini 2.5 Computer Use Preview model for Android UI automation.

## Features

- Uses Gemini 2.5 Computer Use Preview model (`gemini-2.5-computer-use-preview-10-2025`)
- Android Accessibility Service for UI automation
- Screenshot capture and analysis
- Natural language task execution
- Supports UI actions: tap, type, scroll, navigation

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio (latest version)
- Gemini API key from [Google AI Studio](https://aistudio.google.com/apikey)
- Internet connection for API calls

## Setup & Build

### ⚡ Option 1: GitHub Actions (Easiest - Builds APK automatically!)

**No computer needed! Build in the cloud:**

1. **Push to GitHub:**
   ```bash
   cd ~/android-gemini-agent
   ./push-to-github.sh
   ```

2. **Add API Key Secret:**
   - Go to: Repository → Settings → Secrets → Actions
   - Click "New repository secret"
   - Name: `GEMINI_API_KEY`
   - Value: Your Gemini API key from https://aistudio.google.com/apikey

3. **Trigger Build:**
   - Actions tab → "Build Android APK" → "Run workflow"
   - Wait ~5-10 minutes

4. **Download APK:**
   - Actions → Latest run → Artifacts → Download `app-debug`

📖 **Detailed guide:** See [GITHUB_SETUP.md](GITHUB_SETUP.md)

### 🖥️ Option 2: Android Studio (Traditional method)

1. **Clone/Copy the project** to your development machine

2. **Get Gemini API Key**:
   - Visit https://aistudio.google.com/apikey
   - Create a new API key

3. **Configure API Key**:
   ```bash
   cp local.properties.example local.properties
   ```
   Edit `local.properties`:
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   sdk.dir=/path/to/your/android/sdk
   ```

4. **Open in Android Studio**:
   - File → Open → Select the `android-gemini-agent` folder
   - Wait for Gradle sync

5. **Build APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   Or: Build → Build Bundle(s) / APK(s) → Build APK(s)

6. **Install on Device**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Usage

1. **Install and Launch** the Gemini Agent app

2. **Enable Accessibility Service**:
   - Tap "Enable Accessibility Service"
   - Find "Gemini Agent" in the list
   - Toggle it ON
   - Accept the permissions

3. **Run a Task**:
   - Enter a task in natural language (e.g., "Open Chrome and search for cats")
   - Tap "Start Agent"
   - Watch the agent execute the task

4. **Stop Anytime**:
   - Tap "Stop Agent" to halt execution

## Example Tasks

- "Open Settings and enable WiFi"
- "Go to Chrome and search for weather"
- "Open the Play Store"
- "Scroll down the current page"
- "Go back to home screen"

## Architecture

```
app/
├── ui/
│   └── MainActivity.kt          - Main UI controller
├── service/
│   └── GeminiAgentService.kt   - Accessibility service for UI automation
├── client/
│   └── GeminiClient.kt         - Gemini API integration
├── models/
│   └── UIAction.kt             - Action data model
└── utils/
    └── AccessibilityUtils.kt   - Accessibility helpers
```

## How It Works

1. **User inputs task** in natural language
2. **Service captures screenshot** of current screen
3. **Sends to Gemini API** with task description
4. **Gemini analyzes screenshot** and returns next action
5. **Service executes action** (tap, type, scroll, etc.)
6. **Loop continues** until task complete or stopped

## Safety & Limitations

- Requires user permission for accessibility service
- Internet connection required (calls Gemini API)
- Model may not understand all UI elements
- Limited to 10 turns per task (configurable)
- Actions are logged in the app

## Troubleshooting

**"Gemini API key not configured"**
- Check `local.properties` file exists and has correct `GEMINI_API_KEY`
- Rebuild the project after adding the key

**"Please enable accessibility service"**
- Go to Settings → Accessibility
- Find "Gemini Agent" and enable it

**Actions not working**
- Check logs in the app
- Ensure accessibility service is enabled
- Try simpler tasks first

**Build errors**
- Ensure Android SDK is properly configured
- Check `local.properties` has correct `sdk.dir`
- Sync Gradle files in Android Studio

## Building from Termux

If you're building directly from Termux on Android:

```bash
# Install Android SDK command-line tools
pkg install gradle openjdk-17

# Set environment variables
export ANDROID_SDK_ROOT=/path/to/sdk
export JAVA_HOME=$PREFIX/opt/openjdk-17

# Build
cd ~/android-gemini-agent
./gradlew assembleDebug
```

## License

MIT License - Feel free to modify and use

## Credits

Built using:
- [Gemini 2.5 Computer Use Preview](https://ai.google.dev/gemini-api/docs/computer-use)
- Android Accessibility Services
- Kotlin & Coroutines
