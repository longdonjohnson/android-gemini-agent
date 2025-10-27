package com.gemini.agent.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.gemini.agent.service.GeminiAgentService

object AccessibilityUtils {
    
    fun isServiceEnabled(context: Context): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(
                    "${context.packageName}/${GeminiAgentService::class.java.name}",
                    ignoreCase = true
                )
            ) {
                return true
            }
        }
        return false
    }
}
