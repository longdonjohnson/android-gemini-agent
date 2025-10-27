package com.gemini.agent.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gemini.agent.R
import com.gemini.agent.databinding.ActivityMainBinding
import com.gemini.agent.service.GeminiAgentService
import com.gemini.agent.utils.AccessibilityUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var geminiAgentService: GeminiAgentService? = null
    private var isBound = false
    private var isAgentRunning = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as GeminiAgentService.LocalBinder
            geminiAgentService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
        }
    }

    companion object {
        private const val PREFS_NAME = "GeminiAgentPrefs"
        private const val KEY_API_KEY = "gemini_api_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkApiKey()
        updateAccessibilityStatus()
    }

    override fun onStart() {
        super.onStart()
        Intent(this, GeminiAgentService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }

    private fun setupUI() {
        binding.startButton.setOnClickListener {
            val task = binding.taskInput.text.toString()
            if (task.isBlank()) {
                Toast.makeText(this, "Please enter a task", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!AccessibilityUtils.isServiceEnabled(this)) {
                Toast.makeText(this, R.string.accessibility_disabled, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!hasApiKey()) {
                showApiKeyDialog()
                return@setOnClickListener
            }

            startAgent(task)
        }

        binding.stopButton.setOnClickListener {
            stopAgent()
        }

        binding.enableAccessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    private fun hasApiKey(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val apiKey = prefs.getString(KEY_API_KEY, "")
        return !apiKey.isNullOrEmpty()
    }

    fun getApiKey(): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    private fun saveApiKey(apiKey: String) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    private fun showApiKeyDialog() {
        val input = EditText(this)
        input.hint = "Enter your Gemini API key"
        
        AlertDialog.Builder(this)
            .setTitle("Gemini API Key Required")
            .setMessage("Get your free API key from:\nhttps://aistudio.google.com/apikey")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val apiKey = input.text.toString().trim()
                if (apiKey.isNotEmpty()) {
                    saveApiKey(apiKey)
                    addLog("API key saved")
                    Toast.makeText(this, "API key saved!", Toast.LENGTH_SHORT).show()
                    binding.startButton.performClick()
                } else {
                    Toast.makeText(this, "API key cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show()
    }

    private fun checkApiKey() {
        if (!hasApiKey()) {
            binding.statusText.text = "API key not configured - will prompt when starting task"
            addLog("No API key found - will prompt on first use")
        } else {
            addLog("API key found")
        }
    }

    private fun updateAccessibilityStatus() {
        val isEnabled = AccessibilityUtils.isServiceEnabled(this)
        binding.enableAccessibilityButton.isEnabled = !isEnabled
        
        if (isEnabled) {
            addLog("Accessibility service enabled")
        } else {
            addLog("Accessibility service disabled - please enable it")
        }
    }

    private fun startAgent(task: String) {
        isAgentRunning = true
        binding.statusText.text = getString(R.string.status_running)
        binding.startButton.isEnabled = false
        binding.stopButton.isEnabled = true
        binding.taskInput.isEnabled = false

        addLog("Starting agent with task: $task")

        if (isBound) {
            geminiAgentService?.startAgentTask(task, window)
        }
    }

    private fun stopAgent() {
        isAgentRunning = false
        binding.statusText.text = getString(R.string.status_idle)
        binding.startButton.isEnabled = true
        binding.stopButton.isEnabled = false
        binding.taskInput.isEnabled = true

        addLog("Stopping agent")

        if (isBound) {
            geminiAgentService?.stopCurrentTask()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    private fun addLog(message: String) {
        val currentLog = binding.logText.text.toString()
        val timestamp = android.text.format.DateFormat.format("HH:mm:ss", System.currentTimeMillis())
        val newLog = "[$timestamp] $message\n$currentLog"
        binding.logText.text = newLog
    }
}
