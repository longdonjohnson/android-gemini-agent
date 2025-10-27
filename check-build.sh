#!/bin/bash

clear
echo "════════════════════════════════════════════════════════"
echo "  Build Status Checker"
echo "════════════════════════════════════════════════════════"
echo ""

# Check if build is running
BUILD_RUNNING=$(ps aux | grep gradle | grep -v grep | wc -l)

if [ $BUILD_RUNNING -gt 0 ]; then
    echo "✅ Build is currently RUNNING"
    echo ""
    ps aux | grep gradle | grep -v grep | head -3
    echo ""
else
    echo "⏸️  No active build process"
    echo ""
fi

# Check log file
if [ -f build.log ]; then
    LOG_SIZE=$(ls -lh build.log | awk '{print $5}')
    echo "📄 Build log: $LOG_SIZE"
    echo ""
    echo "Last 15 lines:"
    echo "────────────────────────────────────────────────────────"
    tail -15 build.log
    echo "────────────────────────────────────────────────────────"
    echo ""
    
    # Check for completion
    if grep -q "BUILD SUCCESSFUL" build.log; then
        echo "🎉 BUILD SUCCESSFUL!"
        echo ""
        if [ -f app/build/outputs/apk/debug/app-debug.apk ]; then
            APK_SIZE=$(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')
            echo "APK Location: app/build/outputs/apk/debug/app-debug.apk"
            echo "APK Size: $APK_SIZE"
        fi
    elif grep -q "BUILD FAILED" build.log; then
        echo "❌ BUILD FAILED"
        echo ""
        echo "Error summary:"
        grep -A 5 "FAILURE\|error:" build.log | tail -20
    else
        echo "⏳ Build in progress..."
    fi
else
    echo "📄 No build log found yet"
fi

echo ""
echo "════════════════════════════════════════════════════════"
echo ""
echo "Commands:"
echo "  Watch live: tail -f build.log"
echo "  Full output: cat build-output.txt"
echo "  Re-check: ./check-build.sh"
echo ""
