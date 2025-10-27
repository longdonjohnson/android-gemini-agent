#!/bin/bash

echo "════════════════════════════════════════════════════════"
echo "  Building Android Gemini Agent APK in Termux"
echo "════════════════════════════════════════════════════════"
echo ""

# Check if local.properties exists and has API key
if [ ! -f "local.properties" ]; then
    echo "❌ local.properties not found!"
    echo "Creating from template..."
    cp local.properties.example local.properties
fi

# Check for API key
if grep -q "placeholder_key_replace_with_real_key" local.properties 2>/dev/null; then
    echo ""
    echo "⚠️  WARNING: Gemini API key not set!"
    echo ""
    echo "To set your API key:"
    echo "1. Get key from: https://aistudio.google.com/apikey"
    echo "2. Edit local.properties"
    echo "3. Replace 'placeholder_key_replace_with_real_key' with your actual key"
    echo ""
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "📦 Checking dependencies..."
if ! command -v gradle &> /dev/null; then
    echo "❌ Gradle not found. Installing..."
    pkg install gradle -y
fi

if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Installing..."
    pkg install openjdk-17 -y
fi

echo ""
echo "✅ Dependencies OK"
echo ""
echo "🔨 Starting build..."
echo "   This may take 10-20 minutes on first run (downloading dependencies)"
echo "   Subsequent builds will be faster (~2-5 minutes)"
echo ""
echo "   Build log: build.log"
echo "   You can monitor with: tail -f build.log"
echo ""

# Run build in background and show progress
gradle assembleDebug --warning-mode all 2>&1 | tee build.log &
BUILD_PID=$!

# Show spinner while building
spin='-\|/'
i=0
while kill -0 $BUILD_PID 2>/dev/null; do
  i=$(( (i+1) %4 ))
  printf "\r${spin:$i:1} Building..."
  sleep 0.5
done

wait $BUILD_PID
BUILD_RESULT=$?

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
        echo "📱 APK built successfully!"
        echo ""
        echo "   Location: $APK_PATH"
        echo "   Size: $APK_SIZE"
        echo ""
        echo "To install:"
        echo "   1. Transfer APK to your Android device"
        echo "   2. Open file manager and tap the APK"
        echo "   3. Enable 'Install from Unknown Sources' if prompted"
        echo "   4. Install and enable accessibility service"
        echo ""
        echo "Or install via ADB:"
        echo "   adb install $APK_PATH"
        echo ""
    else
        echo "⚠️  Build succeeded but APK not found at expected location"
        echo "Check: app/build/outputs/apk/"
    fi
else
    echo "════════════════════════════════════════════════════════"
    echo "  ❌ BUILD FAILED"
    echo "════════════════════════════════════════════════════════"
    echo ""
    echo "Check build.log for details:"
    echo "   tail -100 build.log"
    echo ""
    echo "Common issues:"
    echo "   • Missing Android SDK: Set sdk.dir in local.properties"
    echo "   • Network error: Check internet connection"
    echo "   • Gradle version: Try 'gradle --version'"
    echo ""
fi

echo "════════════════════════════════════════════════════════"
