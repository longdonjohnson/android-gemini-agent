package com.gemini.agent.client

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.gemini.agent.models.UIAction
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException

class GeminiClient(private val context: Context) {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    
    private fun getApiKey(): String {
        val prefs = context.getSharedPreferences("GeminiAgentPrefs", Context.MODE_PRIVATE)
        return prefs.getString("gemini_api_key", "") ?: ""
    }
    
    companion object {
        private const val TAG = "GeminiClient"
        private const val MODEL = "gemini-2.5-computer-use-preview-10-2025"
        private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
    }

    suspend fun getNextAction(task: String, screenshot: Bitmap, isFirstTurn: Boolean): UIAction? {
        return withContext(Dispatchers.IO) {
            try {
                val request = buildRequest(task, screenshot, isFirstTurn)
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "API request failed: ${response.code}")
                    return@withContext null
                }

                val responseBody = response.body?.string()
                Log.d(TAG, "Response: $responseBody")

                parseResponse(responseBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error calling Gemini API", e)
                null
            }
        }
    }

    private fun buildRequest(task: String, screenshot: Bitmap, isFirstTurn: Boolean): Request {
        val imageBase64 = bitmapToBase64(screenshot)
        
        val prompt = if (isFirstTurn) {
            "You are an Android automation agent. Execute this task: $task\n\n" +
            "Available actions:\n" +
            "- tap(x, y): Tap at coordinates\n" +
            "- type(text): Type text\n" +
            "- scroll(direction): Scroll up or down\n" +
            "- wait(ms): Wait\n" +
            "- back(): Press back\n" +
            "- home(): Go home\n\n" +
            "Respond in JSON format: {\"action\": \"tap|type|scroll|wait|back|home\", \"x\": 0-999, \"y\": 0-999, \"text\": \"...\", \"direction\": \"up|down\", \"duration\": 0, \"complete\": false, \"message\": \"...\"}"
        } else {
            "Continue the task. What's the next action?"
        }

        val requestBody = JsonObject().apply {
            add("contents", gson.toJsonTree(listOf(
                mapOf(
                    "role" to "user",
                    "parts" to listOf(
                        mapOf("text" to prompt),
                        mapOf(
                            "inline_data" to mapOf(
                                "mime_type" to "image/png",
                                "data" to imageBase64
                            )
                        )
                    )
                )
            )))

            add("tools", gson.toJsonTree(listOf(
                mapOf(
                    "function_declarations" to listOf(
                        mapOf(
                            "name" to "perform_action",
                            "description" to "Performs a UI action on the device",
                            "parameters" to mapOf(
                                "type" to "object",
                                "properties" to mapOf(
                                    "action" to mapOf(
                                        "type" to "string",
                                        "description" to "The action to perform"
                                    ),
                                    "x" to mapOf(
                                        "type" to "integer",
                                        "description" to "The x-coordinate"
                                    ),
                                    "y" to mapOf(
                                        "type" to "integer",
                                        "description" to "The y-coordinate"
                                    ),
                                    "text" to mapOf(
                                        "type" to "string",
                                        "description" to "The text to type"
                                    ),
                                    "direction" to mapOf(
                                        "type" to "string",
                                        "description" to "The direction to scroll"
                                    ),
                                    "duration" to mapOf(
                                        "type" to "integer",
                                        "description" to "The duration to wait"
                                    ),
                                    "complete" to mapOf(
                                        "type" to "boolean",
                                        "description" to "Whether the task is complete"
                                    ),
                                    "message" to mapOf(
                                        "type" to "string",
                                        "description" to "A message to the user"
                                    )
                                )
                            )
                        )
                    )
                )
            )))
            
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.1)
                addProperty("maxOutputTokens", 1000)
            })
        }

        val body = requestBody.toString()
            .toRequestBody("application/json".toMediaType())

        val apiKey = getApiKey()
        return Request.Builder()
            .url("$API_URL?key=$apiKey")
            .post(body)
            .build()
    }

    private fun parseResponse(responseBody: String?): UIAction? {
        if (responseBody == null) return null

        try {
            val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
            val candidates = jsonResponse.getAsJsonArray("candidates")
            if (candidates == null || candidates.size() == 0) {
                Log.e(TAG, "No candidates in response")
                return null
            }

            val content = candidates[0].asJsonObject
                .getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            val functionCall = parts[0].asJsonObject.getAsJsonObject("functionCall")
            val args = functionCall.getAsJsonObject("args")

            return UIAction(
                type = args.get("action")?.asString ?: "wait",
                x = denormalizeCoord(args.get("x")?.asInt ?: 0, 1080),
                y = denormalizeCoord(args.get("y")?.asInt ?: 0, 2400),
                text = args.get("text")?.asString,
                direction = args.get("direction")?.asString,
                duration = args.get("duration")?.asLong ?: 2000,
                isComplete = args.get("complete")?.asBoolean ?: false,
                message = args.get("message")?.asString
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing response", e)
            return createDefaultAction()
        }
    }

    private fun denormalizeCoord(normalized: Int, screenDimension: Int): Int {
        return (normalized * screenDimension) / 1000
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun createDefaultAction(): UIAction {
        return UIAction(
            type = "wait",
            duration = 2000,
            message = "Waiting..."
        )
    }
}
