#!/bin/bash

# Build script with Termux notifications and wakelock

PROJECT_NAME="Gemini Agent"
NOTIFICATION_ID="build_apk"

# Check if termux-notification is available
HAS_NOTIFICATIONS=false
if command -v termux-notification &> /dev/null; then
    HAS_NOTIFICATIONS=true
fi

# Acquire wakelock to prevent sleep during build
if command -v termux-wake-lock &> /dev/null; then
    termux-wake-lock
    echo "🔒 Wakelock acquired - device won't sleep during build"
fi

# Function to release wakelock on exit
cleanup() {
    if command -v termux-wake-unlock &> /dev/null; then
        termux-wake-unlock
        echo "🔓 Wakelock released"
    fi
}
trap cleanup EXIT

notify() {
    local title="$1"
    local message="$2"
    
    echo "[$title] $message"
    
    if [ "$HAS_NOTIFICATIONS" = true ]; then
        termux-notification \
            --id "$NOTIFICATION_ID" \
            --title "$title" \
            --content "$message" \
            --ongoing \
            --priority high
    fi
}

notify_done() {
    local title="$1"
    local message="$2"
    
    echo "[$title] $message"
    
    if [ "$HAS_NOTIFICATIONS" = true ]; then
        termux-notification \
            --id "$NOTIFICATION_ID" \
            --title "$title" \
            --content "$message" \
            --priority high \
            --vibrate 200,200,200
        termux-notification-remove "$NOTIFICATION_ID"
    fi
}

clear
echo "════════════════════════════════════════════════════════"
echo "  Building $PROJECT_NAME APK"
echo "════════════════════════════════════════════════════════"
echo ""

notify "Build Starting" "Preparing to build $PROJECT_NAME..."

# Check dependencies
echo "📦 Checking dependencies..."
if ! command -v gradle &> /dev/null; then
    notify "Installing Gradle" "Gradle not found, installing..."
    pkg install gradle -y
fi

if ! command -v java &> /dev/null; then
    notify "Installing Java" "Java not found, installing..."
    pkg install openjdk-17 -y
fi

notify "Build Started" "Compiling APK... This may take 10-20 minutes"

echo ""
echo "✅ Dependencies OK"
echo ""
echo "🔨 Starting build..."
echo "   First build: 10-20 minutes (downloading dependencies)"
echo "   Subsequent builds: 2-5 minutes"
echo ""
echo "   Progress will show in notifications"
echo "   Full log: build.log"
echo ""

# Start build
START_TIME=$(date +%s)

gradle assembleDebug --warning-mode all > build.log 2>&1
BUILD_RESULT=$?

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

echo ""
echo ""

if [ $BUILD_RESULT -eq 0 ]; then
    echo "════════════════════════════════════════════════════════"
    echo "  ✅ BUILD SUCCESSFUL!"
    echo "════════════════════════════════════════════════════════"
    echo ""
    
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    
    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(ls -lh "$APK_PATH" | awk '{print $5}')
        
        notify_done "Build Complete!" "APK ready (${APK_SIZE}) in ${MINUTES}m ${SECONDS}s"
        
        echo "📱 APK built successfully!"
        echo ""
        echo "   Location: $APK_PATH"
        echo "   Size: $APK_SIZE"
        echo "   Build time: ${MINUTES}m ${SECONDS}s"
        echo ""
        echo "📥 To install:"
        echo "   1. Copy APK to storage/downloads"
        echo "   2. Open file manager and tap APK"
        echo "   3. Enable 'Install from Unknown Sources' if prompted"
        echo ""
        echo "Or via ADB:"
        echo "   adb install $APK_PATH"
        echo ""
        
        # Offer to copy to storage
        if [ -d ~/storage/downloads ]; then
            echo "Copy to ~/storage/downloads for easy access? (y/n)"
            read -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                cp "$APK_PATH" ~/storage/downloads/gemini-agent-debug.apk
                echo "✅ Copied to ~/storage/downloads/gemini-agent-debug.apk"
                notify_done "APK Ready" "Find it in your Downloads folder"
            fi
        fi
    else
        notify_done "Build Succeeded" "But APK not found at expected location"
        echo "⚠️  Build succeeded but APK not found"
        echo "Check: app/build/outputs/apk/"
    fi
else
    notify_done "Build Failed" "Check build.log for details"
    
    echo "════════════════════════════════════════════════════════"
    echo "  ❌ BUILD FAILED"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "Last 50 lines of build log:"
    echo "────────────────────────────────────────────────────────"
    tail -50 build.log
    echo "────────────────────────────────────────────────────────"
    echo ""
    echo "Full log: build.log"
    echo ""
    echo "Common issues:"
    echo "   • Network error: Check internet connection"
    echo "   • Out of memory: Close other apps"
    echo "   • Gradle cache: Try 'gradle clean'"
    echo ""
fi

echo "════════════════════════════════════════════════════════"
