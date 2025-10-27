package com.example.leo.ai

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * First reasoning engine for Leo.
 * Offline, Charter-safe, and context-aware.
 */
object BrainsEngine : ChatEngine {
    override suspend fun reply(
        history: List<Pair<Boolean, String>>,
        userText: String
    ): String {
        val input = userText.trim()
        if (input.isBlank()) return "Try typing a little more — I’m listening."

        val lower = input.lowercase()

        // Greetings
        if (listOf("hi", "hey", "hello", "yo", "howdy", "good morning", "good evening")
                .any { lower.startsWith(it) }) {
            return "Hey there! What’s on your mind?"
        }

        // Simple math (safe, basic)
        if (Regex("""^[0-9\s+\-*/().]+$""").matches(lower) && input.any { it.isDigit() }) {
            return try {
                val result = evalMath(lower)
                "Looks like $input = $result"
            } catch (_: Exception) {
                "That looks like math, but I couldn’t quite solve it safely."
            }
        }

        // Time/date
        if (lower.contains("time") || lower.contains("date") || lower.contains("day")) {
            val now = LocalDateTime.now()
            val formatted = now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a"))
            return "It’s $formatted right now."
        }

        // Charter mention
        if (lower.contains("charter")) {
            return "The Charter is my compass: I never mislead the children. That rule can’t be broken."
        }

        // Reflective fallback
        val lastUser = history.lastOrNull { it.first }?.second.orEmpty()
        return if (lastUser.isNotBlank())
            "You mentioned “$lastUser” earlier — could you tell me a bit more about that?"
        else
            "I’m thinking… tell me a bit more about what you mean by “$input.”"
    }

    private fun evalMath(expr: String): Double {
        val cleaned = expr.replace(Regex("[^0-9+\\-*/().]"), "")
        return object {
            var i = -1
            var ch = 0
            fun nextChar() { ch = if (++i < cleaned.length) cleaned[i].code else -1 }
            fun eat(c: Int): Boolean { while (ch == ' '.code) nextChar(); return if (ch == c) { nextChar(); true } else false }
            fun parse(): Double { nextChar(); val x = parseExpr(); if (i < cleaned.length) error("Unexpected"); return x }
            fun parseExpr(): Double { var x = parseTerm(); while (true) { when { eat('+'.code) -> x += parseTerm(); eat('-'.code) -> x -= parseTerm(); else -> return x } } }
            fun parseTerm(): Double { var x = parseFactor(); while (true) { when { eat('*'.code) -> x *= parseFactor(); eat('/'.code) -> x /= parseFactor(); else -> return x } } }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                val start = i
                val x = if (eat('('.code)) { val v = parseExpr(); eat(')'.code); v }
                else {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    cleaned.substring(start, i).toDouble()
                }
                return x
            }
        }.parse()
    }
}