package com.gemini.agent.models

data class UIAction(
    val type: String,                    // tap, type, scroll, wait, back, home, navigate
    val x: Int = 0,                      // denormalized (e.g., 0-1080)
    val y: Int = 0,                      // denormalized (e.g., 0-2400)
    val text: String? = null,            // for type action
    val direction: String? = null,       // for scroll: up, down
    val duration: Long = 2000,           // for wait action (ms)
    val isComplete: Boolean = false,     // task completion flag
    val message: String? = null          // status message
)
