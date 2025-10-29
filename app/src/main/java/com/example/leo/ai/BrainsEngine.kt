package com.example.leo.ai

import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.math.MathContext
import java.util.Locale
import kotlin.math.min

/**
 * BrainsEngine — tiny, offline-friendly conversation engine.
 *
 * Contract:
 *   suspend fun reply(history: List<Pair<Boolean, String>>, userText: String): String
 *
 * history = list of (isUser, text) from oldest->newest
 * userText = the latest user message
 *
 * Goals:
 *  - Don’t parrot the user’s words.
 *  - Keep a hint of context (last few turns).
 *  - Handle quick math (+ - * /) safely.
 *  - Be concise and offer a relevant follow-up.
 */
class BrainsEngine : ChatEngine {

    override suspend fun reply(
        history: List<Pair<Boolean, String>>,
        userText: String
    ): String {
        val text = userText.trim()
        if (text.isBlank()) return "Say a thing and I’ll meet you there."

        // Small “typing” pause
        delay(120)

        val recent = squeezeContext(history, keep = 4)
        val lower = text.lowercase(Locale.US)

        // Avoid echo if previous assistant line overlaps too much
        if (looksLikeEcho(recent, text)) {
            return short(
                "I caught that. What’s the core question under that?",
                follow("Give me one concrete detail you want help with.")
            )
        }

        // Greetings
        if (isGreeting(lower)) {
            val lastTopic = lastUserTopic(recent)
            return if (lastTopic != null) {
                short("Hey — want to keep going on $lastTopic?", follow("Or switch lanes entirely?"))
            } else {
                short("Hey there. What should we tackle first?", follow("Tech, domes, homeschool, or just chat?"))
            }
        }

        // Thanks / parting
        if (isThanks(lower)) {
            return short("Anytime. Did that fully unblock you?", follow("If not, what’s the next snag?"))
        }
        if (isGoodbye(lower)) {
            return short("I’ll be here when you’re back.", follow("Want me to jot a tiny TODO before you go?"))
        }

        // Lightweight math
        if (couldBeMath(lower)) {
            val eval = safeEval(lower)
            if (eval != null) return short(eval, follow("Want a quick rule of thumb from that?"))
        }

        // Domain nudges (gentle)
        domainNudge(lower)?.let { nudge ->
            return short(nudge, follow("Want a 3-step plan or a checklist?"))
        }

        // Default: reflect + nudge specificity
        val probe = pickProbeFor(lower)
        return short(reflect(text), follow(probe))
    }

    // ---------- helpers ----------

    private fun short(main: String, follow: String): String =
        if (follow.isBlank()) main else "$main\n\n$follow"

    private fun follow(t: String) = "→ $t"

    private fun looksLikeEcho(history: List<Pair<Boolean, String>>, user: String): Boolean {
        val lastBot = history.lastOrNull { !it.first }?.second ?: return false
        val a = normalize(user)
        val b = normalize(lastBot)
        if (a.isEmpty() || b.isEmpty()) return false
        val overlap = jaccard(a, b)
        return overlap >= 0.85
    }

    private fun normalize(s: String): Set<String> =
        s.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

    private fun jaccard(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() && b.isEmpty()) return 1.0
        val inter = a.intersect(b).size.toDouble()
        val union = (a union b).size.toDouble()
        return if (union == 0.0) 0.0 else inter / union
    }

    private fun squeezeContext(history: List<Pair<Boolean, String>>, keep: Int): List<Pair<Boolean, String>> {
        val n = min(keep, history.size)
        return history.takeLast(n)
    }

    private fun lastUserTopic(history: List<Pair<Boolean, String>>): String? {
        val lastUser = history.lastOrNull { it.first }?.second ?: return null
        return lastUser
            .lowercase(Locale.US)
            .split(Regex("\\W+"))
            .firstOrNull { it.length in 4..14 }
    }

    // intents
    private fun isGreeting(s: String) =
        s.matches(Regex("^(hi|hey|hello|yo|howdy|good\\s*(morning|afternoon|evening)).*"))

    private fun isThanks(s: String) =
        s.contains("thanks") || s.contains("thank you") || s.contains("thx")

    private fun isGoodbye(s: String) =
        Regex("\\b(bye|gtg|good night|see ya|laters|signing off)\\b").containsMatchIn(s)

    private fun couldBeMath(s: String) =
        Regex("^[\\s\\d+\\-*/().xX]+$").matches(s) || Regex("\\d\\s*(\\+|\\-|\\*|/|x)\\s*\\d").containsMatchIn(s)

    // simple four-op evaluator: integers/decimals/( )
    private fun safeEval(expr: String): String? {
        return try {
            val tokens = tokenize(expr.replace('x', '*').replace('X', '*'))
            val value = parseExpression(tokens)
            if (tokens.position != tokens.items.size) return null
            value.stripTrailingZeros().toPlainString()
        } catch (_: Exception) {
            null
        }
    }

    private class Tokens(val items: List<String>, var position: Int = 0)
    private fun tokenize(s: String): Tokens {
        val out = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c in "+-*/()" -> { out += c.toString(); i++ }
                c.isDigit() || c == '.' -> {
                    val start = i
                    i++
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    out += s.substring(start, i)
                }
                else -> throw IllegalArgumentException("bad token")
            }
        }
        return Tokens(out)
    }

    private fun parseExpression(t: Tokens): BigDecimal {
        var v = parseTerm(t)
        while (t.position < t.items.size) {
            when (t.items[t.position]) {
                "+" -> { t.position++; v = v.add(parseTerm(t), MC) }
                "-" -> { t.position++; v = v.subtract(parseTerm(t), MC) }
                else -> return v
            }
        }
        return v
    }

    private fun parseTerm(t: Tokens): BigDecimal {
        var v = parseFactor(t)
        while (t.position < t.items.size) {
            when (t.items[t.position]) {
                "*" -> { t.position++; v = v.multiply(parseFactor(t), MC) }
                "/" -> {
                    t.position++
                    val d = parseFactor(t)
                    if (d.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
                    v = v.divide(d, MC)
                }
                else -> return v
            }
        }
        return v
    }

    private fun parseFactor(t: Tokens): BigDecimal {
        if (t.position >= t.items.size) error("unexpected end")
        val tok = t.items[t.position]
        return when (tok) {
            "+" -> { t.position++; parseFactor(t) }
            "-" -> { t.position++; parseFactor(t).negate(MC) }
            "(" -> {
                t.position++
                val inner = parseExpression(t)
                if (t.position >= t.items.size || t.items[t.position] != ")") error("missing )")
                t.position++
                inner
            }
            else -> {
                t.position++
                tok.toBigDecimalOrNull() ?: error("not a number")
            }
        }
    }

    private val MC = MathContext(18)

    private fun domainNudge(s: String): String? {
        return when {
            "dome" in s || "aircrete" in s ->
                "For the dome flow, choose first: people-flow or plumbing-simplicity. That choice dictates doorway placement and merge arcs."
            "aquarium" in s || "betta" in s || "cory" in s ->
                "Aquarium sanity: stable temp + surplus hides + over-filtration. Trim feeding by ~20% and watch behavior settle."
            "ellie" in s || "reading" in s ->
                "For Ellie: 8-minute read-aloud, 2-minute doodle recap, then one question she asks you. Keep wins tiny and visible."
            else -> null
        }
    }

    private fun reflect(user: String): String {
        val cleaned = user.trim().replace(Regex("\\s+"), " ")
        return if (cleaned.length < 12) "Noted. Tell me one layer deeper."
        else "So the gist is: ${cleaned.take(140)}"
    }

    private fun pickProbeFor(s: String): String {
        return when {
            "plan" in s || "steps" in s -> "Want a 3-step version?"
            "stuck" in s || "blocked" in s -> "What’s the tiniest next action you’d actually do?"
            "idea" in s || "concept" in s -> "Should we stress-test it or prototype quickly?"
            "error" in s || "build" in s || "gradle" in s -> "Paste the first error line; we’ll slice from there."
            else -> "Should this be advice, a checklist, or just a sounding board?"
        }
    }
}
