package com.example.leo.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

// ---- Serialization config (modern) ----
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = false
}

// ---- Storage file name ----
private const val CHAT_FILE = "chat_history.json"

// ---- Models ----
@Serializable
data class ChatRecord(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val ts: Long,
    val status: SyncStatus
)

@Serializable
enum class SyncStatus { Pending, Sent, Failed }

// ---- Store API ----
object ChatStore {

    suspend fun read(ctx: Context): List<ChatRecord> = withContext(Dispatchers.IO) {
        val file = ctx.getFileStreamPath(CHAT_FILE)
        if (!file.exists()) return@withContext emptyList()

        try {
            val payload = file.readText()
            if (payload.isBlank()) emptyList()
            else json.decodeFromString(payload) // <— no ListSerializer needed
        } catch (e: Exception) {
            // Corrupt or old format? Start fresh instead of crashing.
            emptyList()
        }
    }

    suspend fun append(ctx: Context, record: ChatRecord) = withContext(Dispatchers.IO) {
        val current = read(ctx).toMutableList()
        current.add(record)
        writeAll(ctx, current)
    }

    suspend fun updateStatus(ctx: Context, id: Long, status: SyncStatus) = withContext(Dispatchers.IO) {
        val current = read(ctx).map { if (it.id == id) it.copy(status = status) else it }
        writeAll(ctx, current)
    }

    suspend fun delete(ctx: Context, id: Long) = withContext(Dispatchers.IO) {
        val current = read(ctx).filterNot { it.id == id }
        writeAll(ctx, current)
    }

    suspend fun clear(ctx: Context) = withContext(Dispatchers.IO) {
        try {
            ctx.deleteFile(CHAT_FILE)
        } catch (_: IOException) {
            // no-op
        }
    }

    // ---- internal ----
    private fun writeAll(ctx: Context, items: List<ChatRecord>) {
        val payload = json.encodeToString(items) // <— generics infer List<ChatRecord>
        ctx.openFileOutput(CHAT_FILE, Context.MODE_PRIVATE).use { out ->
            out.write(payload.toByteArray())
        }
    }
}
