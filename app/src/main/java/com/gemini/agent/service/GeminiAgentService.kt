package com.gemini.agent.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat.getSystemService
import com.gemini.agent.client.GeminiClient
import com.gemini.agent.models.UIAction
import kotlinx.coroutines.*

class GeminiAgentService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var geminiClient: GeminiClient? = null
    private var isTaskRunning = false
    private var currentTask: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private val binder = LocalBinder()

    private var screenWidth = 0
    private var screenHeight = 0

    inner class LocalBinder : Binder() {
        fun getService(): GeminiAgentService = this@GeminiAgentService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    companion object {
        private const val TAG = "GeminiAgentService"
        private const val EXTRA_TASK = "task"
        private const val ACTION_START_TASK = "START_TASK"
        private const val ACTION_STOP_TASK = "STOP_TASK"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        geminiClient = GeminiClient(applicationContext)
        getScreenDimensions()
        Log.d(TAG, "Service created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service connected")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TASK -> {
                val task = intent.getStringExtra(EXTRA_TASK)
                if (task != null) {
                    startAgentTask(task)
                }
            }
            ACTION_STOP_TASK -> {
                stopCurrentTask()
            }
        }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitor accessibility events if needed
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
        stopCurrentTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        scope.cancel()
        Log.d(TAG, "Service destroyed")
    }

    private fun getScreenDimensions() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels
        Log.d(TAG, "Screen: ${screenWidth}x${screenHeight}")
    }

    fun startAgentTask(task: String, window: android.view.Window) {
        if (isTaskRunning) {
            Log.w(TAG, "Task already running")
            return
        }

        currentTask = task
        isTaskRunning = true
        Log.d(TAG, "Starting task: $task")

        scope.launch {
            runAgentLoop(task, window)
        }
    }

    fun stopCurrentTask() {
        isTaskRunning = false
        currentTask = null
        Log.d(TAG, "Task stopped")
    }

    private suspend fun runAgentLoop(task: String, window: android.view.Window) {
        var turnCount = 0
        val maxTurns = 10

        try {
            while (isTaskRunning && turnCount < maxTurns) {
                turnCount++
                Log.d(TAG, "Agent turn $turnCount")

                // Capture screenshot
                val screenshot = captureScreenshot(window)
                if (screenshot == null) {
                    Log.e(TAG, "Failed to capture screenshot")
                    delay(2000)
                    continue
                }

                // Get action from Gemini
                val action = geminiClient?.getNextAction(task, screenshot, turnCount == 1)
                if (action == null) {
                    Log.e(TAG, "Failed to get action from Gemini")
                    delay(2000)
                    continue
                }

                Log.d(TAG, "Action: ${action.type}")

                // Execute action
                val success = executeAction(action)
                if (!success) {
                    Log.e(TAG, "Failed to execute action")
                }

                // Check if task is complete
                if (action.isComplete) {
                    Log.d(TAG, "Task completed: ${action.message}")
                    stopCurrentTask()
                    break
                }

                // Wait before next action
                delay(1500)
            }

            if (turnCount >= maxTurns) {
                Log.w(TAG, "Max turns reached")
                stopCurrentTask()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in agent loop", e)
            stopCurrentTask()
        }
    }

    private suspend fun captureScreenshot(window: android.view.Window): Bitmap? = suspendCancellableCoroutine { continuation ->
        val bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888)
        val locationOfViewInWindow = IntArray(2)
        window.decorView.getLocationInWindow(locationOfViewInWindow)

        PixelCopy.request(
            window,
            android.graphics.Rect(0, 0, screenWidth, screenHeight),
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    continuation.resume(bitmap, null)
                } else {
                    Log.e(TAG, "Screenshot failed: $result")
                    continuation.resume(null, null)
                }
            },
            handler
        )
    }

    private fun executeAction(action: UIAction): Boolean {
        return when (action.type) {
            "tap" -> performTap(action.x, action.y)
            "type" -> performType(action.text ?: "")
            "scroll" -> performScroll(action.direction ?: "down")
            "wait" -> performWait(action.duration ?: 2000)
            "back" -> performGlobalAction(GLOBAL_ACTION_BACK)
            "home" -> performGlobalAction(GLOBAL_ACTION_HOME)
            else -> {
                Log.w(TAG, "Unknown action type: ${action.type}")
                false
            }
        }
    }

    private fun performTap(x: Int, y: Int): Boolean {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun performType(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = findFocus(rootNode)

        return if (focusedNode != null && focusedNode.isEditable) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else {
            Log.w(TAG, "No editable view focused for typing")
            false
        }
    }

    private fun findFocus(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    private fun performScroll(direction: String): Boolean {
        val startX = screenWidth / 2
        val startY = screenHeight / 2
        val endX = startX
        val endY = when (direction) {
            "up" -> screenHeight * 3 / 4
            "down" -> screenHeight / 4
            else -> screenHeight / 4
        }

        val path = Path()
        path.moveTo(startX.toFloat(), startY.toFloat())
        path.lineTo(endX.toFloat(), endY.toFloat())

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun performWait(duration: Long): Boolean {
        Thread.sleep(duration)
        return true
    }
}
