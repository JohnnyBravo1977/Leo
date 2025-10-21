package com.example.leo.ai

/**
 * Baseline-safe engine: just echoes the user's text.
 * Lets us plug in a real backend later without changing UI.
 */
class EchoChatEngine : ChatEngine {
    override suspend fun reply(
        history: List<Pair<Boolean, String>>,
        userText: String
    ): String = "You said: $userText"
}