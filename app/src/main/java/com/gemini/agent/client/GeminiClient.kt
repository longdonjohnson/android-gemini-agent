package com.gemini.agent.client

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.gemini.agent.models.UIAction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonObject
import com.google.gson.JsonArray
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
    private val toolDefinition: JsonObject by lazy {
        // Define the Computer Use tool functions as a JSON object
        // This is a simplified version of the full Computer Use toolset for the Android agent
        val clickAt = JsonObject().apply {
            addProperty("name", "click_at")
            addProperty("description", "Clicks at a specific coordinate on the screen. The x and y values are based on a 1000x1000 grid and are scaled to the screen dimensions.")
            add("parameters", JsonObject().apply {
                addProperty("type", "object")
                add("properties", JsonObject().apply {
                    add("x", JsonObject().apply { addProperty("type", "integer"); addProperty("description", "The x-coordinate (0-999) of the click position.") })
                    add("y", JsonObject().apply { addProperty("type", "integer"); addProperty("description", "The y-coordinate (0-999) of the click position.") })
                })
                add("required", JsonArray().apply { add("x"); add("y") })
            })
        }

        val typeTextAt = JsonObject().apply {
            addProperty("name", "type_text_at")
            addProperty("description", "Types text at a specific coordinate. x and y are based on a 1000x1000 grid.")
            add("parameters", JsonObject().apply {
                addProperty("type", "object")
                add("properties", JsonObject().apply {
                    add("x", JsonObject().apply { addProperty("type", "integer"); addProperty("description", "The x-coordinate (0-999) of the text field.") })
                    add("y", JsonObject().apply { addProperty("type", "integer"); addProperty("description", "The y-coordinate (0-999) of the text field.") })
                    add("text", JsonObject().apply { addProperty("type", "string"); addProperty("description", "The text to input into the field.") })
                })
                add("required", JsonArray().apply { add("x"); add("y"); add("text") })
            })
        }

        val scrollDocument = JsonObject().apply {
            addProperty("name", "scroll_document")
            addProperty("description", "Scrolls the entire screen or current view in a specified direction.")
            add("parameters", JsonObject().apply {
                addProperty("type", "object")
                add("properties", JsonObject().apply {
                    add("direction", JsonObject().apply { addProperty("type", "string"); addProperty("description", "The direction to scroll: 'up', 'down', 'left', or 'right'.") })
                })
                add("required", JsonArray().apply { add("direction") })
            })
        }

        val goBack = JsonObject().apply {
            addProperty("name", "go_back")
            addProperty("description", "Navigates to the previous screen or page.")
            add("parameters", JsonObject().apply { addProperty("type", "object"); add("properties", JsonObject()) })
        }

        val search = JsonObject().apply {
            addProperty("name", "search")
            addProperty("description", "Navigates to the default search engine's homepage. Useful for starting a new search task.")
            add("parameters", JsonObject().apply { addProperty("type", "object"); add("properties", JsonObject()) })
        }

        val navigate = JsonObject().apply {
            addProperty("name", "navigate")
            addProperty("description", "Navigates the browser directly to the specified URL.")
            add("parameters", JsonObject().apply {
                addProperty("type", "object")
                add("properties", JsonObject().apply {
                    add("url", JsonObject().apply { addProperty("type", "string"); addProperty("description", "The URL to navigate to.") })
                })
                add("required", JsonArray().apply { add("url") })
            })
        }

        // The final tools structure
        JsonObject().apply {
            add("tools", JsonArray().apply {
                add(JsonObject().apply {
                    add("functionDeclarations", JsonArray().apply {
                        add(clickAt)
                        add(typeTextAt)
                        add(scrollDocument)
                        add(goBack)
                        add(search)
                        add(navigate)
                    })
                })
            })
        }
    }
    
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
                Log.d(TAG, "Raw Response: $responseBody")

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
            "You are an Android automation agent. Execute this task: $task. Use the provided tools to interact with the screen."
        } else {
            "Continue the task. What's the next action? Use the provided tools to interact with the screen."
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
            
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.1)
                addProperty("maxOutputTokens", 1000)
            })
            // Add the tool definition to the request
            add("tools", toolDefinition.getAsJsonArray("tools"))
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

            val content = candidates[0].asJsonObject.getAsJsonObject("content")
            val functionCalls = content.getAsJsonArray("functionCalls")

            if (functionCalls == null || functionCalls.size() == 0) {
                // Check for a text response, which indicates the task is complete or model has a message
                val parts = content.getAsJsonArray("parts")
                val text = parts?.get(0)?.asJsonObject?.get("text")?.asString
                
                if (text != null) {
                    Log.d(TAG, "Model response (text): $text")
                    // If the model provides a text response, it likely means the task is complete
                    return UIAction(
                        type = "wait", // Use wait as a safe default action
                        duration = 500,
                        isComplete = true,
                        message = text
                    )
                }
                
                Log.e(TAG, "No function calls or text in response")
                return createDefaultAction()
            }

            val firstCall = functionCalls[0].asJsonObject
            val functionName = firstCall.get("name").asString
            val args = firstCall.getAsJsonObject("args")
            
            Log.d(TAG, "Model response (function): $functionName with args: $args")

            // Map the official function call to the internal UIAction model
            return when (functionName) {
                "click_at" -> UIAction(
                    type = "tap",
                    x = denormalizeCoord(args.get("x")?.asInt ?: 0, 1080),
                    y = denormalizeCoord(args.get("y")?.asInt ?: 0, 2400)
                )
                "type_text_at" -> UIAction(
                    type = "type",
                    x = denormalizeCoord(args.get("x")?.asInt ?: 0, 1080),
                    y = denormalizeCoord(args.get("y")?.asInt ?: 0, 2400),
                    text = args.get("text")?.asString
                )
                "scroll_document" -> UIAction(
                    type = "scroll",
                    direction = args.get("direction")?.asString
                )
                "go_back" -> UIAction(type = "back")
                "search" -> UIAction(type = "home") // Map 'search' to 'home' for simplicity in this agent
                "navigate" -> UIAction(
                    type = "navigate",
                    text = args.get("url")?.asString // Use 'text' field to pass the URL
                )
                else -> {
                    Log.e(TAG, "Unknown function call: $functionName")
                    createDefaultAction()
                }
            }
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
