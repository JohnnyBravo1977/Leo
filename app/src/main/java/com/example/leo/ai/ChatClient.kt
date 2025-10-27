package com.example.leo.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Minimal chat client.
 * - If AL_API_KEY is blank, route to BrainsEngine for offline reasoning.
 * - If AL_API_KEY is set, POST to the configured endpoint.
 */
class ChatClient {

    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun send(messages: List<Pair<String, String>>): String {
        if (AL_API_KEY.isBlank()) {
            val history: List<Pair<Boolean, String>> = messages.map { (role, content) ->
                role.equals("user", ignoreCase = true) to content
            }
            val userText = messages.lastOrNull { it.first.equals("user", ignoreCase = true) }?.second
                ?: messages.lastOrNull()?.second
                ?: ""
            if (userText.isBlank()) {
                return "Hi, I'm LittleGenius — tell me something and I'll help."
            }
            return withContext(Dispatchers.Default) {
                BrainsEngine.reply(history, userText).ifBlank {
                    "I’m thinking, but didn’t find words yet. Try rephrasing?"
                }
            }
        }

        return withContext(Dispatchers.IO) {
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
                .url("https://example.invalid/chat") // replace later if you add a server
                .addHeader("Authorization", "Bearer $AL_API_KEY")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext "Network error ${resp.code}"
                val body = resp.body?.string().orEmpty()
                return@withContext try {
                    JSONObject(body).optString("reply").ifBlank { "No reply." }
                } catch (_: Exception) { "Couldn't parse reply." }
            }
        }
    }

    companion object {
        // KEEP BLANK for offline BrainsEngine mode.
        private const val AL_API_KEY: String = ""
    }
}
