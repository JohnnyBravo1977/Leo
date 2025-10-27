package com.example.leo.ai

/**
 * Tiny abstraction for any chat backend.
 * history: List of (isUser, text) pairs
 * userText: the latest user message to answer
 */
interface ChatEngine {
    suspend fun reply(history: List<Pair<Boolean, String>>, userText: String): String
}