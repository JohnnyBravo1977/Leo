package com.example.leo.ai

/**
 * Example custom engine demonstrating how to add new response logic.
 * Implements ChatEngine fully.
 * You can expand its reply() for personality, dad jokes, etc.
 */
class SimpleDadEngine : ChatEngine {

    override suspend fun reply(
        history: List<Pair<Boolean, String>>,
        userText: String
    ): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("hi") || lower.contains("hello") -> "Hey kiddo! How’s your day?"
            lower.contains("joke") -> "Why did the fish blush? Because it saw the ocean’s bottom!"
            lower.contains("bye") -> "See ya later, alligator!"
            else -> "I’m just a simple dad bot. You said: \"$userText\""
        }
    }
}