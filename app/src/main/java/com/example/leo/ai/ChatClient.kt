package com.example.leo.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Minimal chat client. If AL_API_KEY is blank, we return a helpful
 * offline fallback so the app keeps working during development.
 */
class ChatClient {

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * @param messages list of (role, content) pairs. Example: ("user", "Hi")
     * @return reply text (offline fallback if no API key)
     */
    suspend fun send(messages: List<Pair<String, String>>): String =
        withContext(Dispatchers.IO) {
            // Offline-friendly fallback while API is disabled
            if (AL_API_KEY.isBlank()) {
                val lastMsg = messages.lastOrNull()?.second.orEmpty()
                return@withContext if (lastMsg.isBlank()) {
                    "Hi, I'm LittleGenius — tell me something and I'll help."
                } else {
                    "You said: \"$lastMsg\". What's a next step?"
                }
            }

            // --- Real request path (won't run unless key is set) ---
            val payload = JSONObject().apply {
                put(
                    "messages",
                    messages.map { (role, content) ->
                        JSONObject().apply {
                            put("role", role)
                            put("content", content)
                        }
                    }
                )
            }

            val request = Request.Builder()
                // Placeholder URL for now; swap in your real endpoint later.
                .url("https://example.invalid/chat")
                .addHeader("Authorization", "Bearer $AL_API_KEY")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    return@withContext "Network error ${resp.code}"
                }
                val body = resp.body?.string().orEmpty()
                return@withContext try {
                    // Expecting {"reply":"..."} — adapt when you wire a real API
                    JSONObject(body).optString("reply").ifBlank { "No reply." }
                } catch (e: Exception) {
                    "Couldn't parse reply."
                }
            }
        }

    companion object {
        // Leave blank for offline mode. Fill in when you wire a real API.
        private const val AL_API_KEY: String = ""
    }
}
