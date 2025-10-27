# Quick Start Guide

## What is this?

An on-device Android AI agent powered by Gemini 2.5 Computer Use model that can automate UI tasks on your phone using natural language commands.

## Features

✅ Natural language task execution
✅ Screenshot analysis
✅ UI automation (tap, type, scroll)
✅ Accessibility service integration
✅ Real-time action execution

## Get Started in 3 Steps

### 1️⃣ Build the APK

**Easiest way:**
- Transfer this folder to a computer with Android Studio
- Open in Android Studio
- Add Gemini API key to `local.properties`
- Build → Build APK

See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for details.

### 2️⃣ Install on Your Phone

```bash
adb install app-debug.apk
```

Or transfer APK and install manually.

### 3️⃣ Use the Agent

1. Launch "Gemini Agent" app
2. Enable Accessibility Service
3. Type: "Open Chrome and search for cats"
4. Tap "Start Agent"
5. Watch it work! 🎉

## Example Tasks

```
"Open Settings and turn on WiFi"
"Go to Play Store and search for games"
"Open Chrome and go to YouTube"
"Scroll down the page"
"Go back to home screen"
```

## Requirements

- Android 8.0+ device
- Gemini API key (free from ai.google.dev)
- Internet connection

## Architecture

```
User Input → Gemini API → Screenshot Analysis → UI Actions → Task Complete
```

## Files Overview

```
app/
├── ui/MainActivity.kt              # Main app interface
├── service/GeminiAgentService.kt  # Accessibility service
├── client/GeminiClient.kt         # Gemini API integration
└── models/UIAction.kt             # Action definitions
```

## Need Help?

- 📖 Read [README.md](README.md) for full documentation
- 🔧 See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for build help
- 🐛 Check logs in the app for debugging

## Safety

- Requires explicit accessibility permissions
- All actions logged in app
- Can stop agent anytime
- Internet required for API calls

---

**Built with ❤️ using Gemini 2.5 Computer Use Preview**
