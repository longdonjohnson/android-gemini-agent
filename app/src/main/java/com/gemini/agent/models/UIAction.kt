package com.gemini.agent.models

data class UIAction(
    val type: String,                    // tap, type, scroll, wait, back, home
    val x: Int = 0,                      // normalized 0-999
    val y: Int = 0,                      // normalized 0-999
    val text: String? = null,            // for type action
    val direction: String? = null,       // for scroll: up, down
    val duration: Long = 2000,           // for wait action (ms)
    val isComplete: Boolean = false,     // task completion flag
    val message: String? = null          // status message
)
