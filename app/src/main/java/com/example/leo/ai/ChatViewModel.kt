package com.example.leo.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.example.leo.ai.ChatEngine
import com.example.leo.ai.BrainsEngine

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val fromUser: Boolean,
    val time: String = DateTimeFormatter.ofPattern("h:mm a").format(LocalDateTime.now()),
    val delivered: Boolean = true
)

class ChatViewModel : ViewModel() {

    private val engine: ChatEngine = BrainsEngine

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()

    fun onInputChange(value: String) { _input.value = value }

    fun sendMessage() {
        val text = _input.value.trim()
        if (text.isEmpty()) return

        val userMsg = ChatMessage(text = text, fromUser = true)
        _messages.value = _messages.value + userMsg
        _input.value = ""

        viewModelScope.launch {
            val history: List<Pair<Boolean, String>> =
                _messages.value.map { it.fromUser to it.text }

            val reply = runCatching {
                engine.reply(history, text).takeIf { it.isNotBlank() }
                    ?: "I’m thinking, but didn’t find words yet. Try rephrasing?"
            }.getOrElse { e ->
                "I hit a snag: ${e.message ?: "unknown error"}. Let’s try again?"
            }

            _messages.value = _messages.value + ChatMessage(
                text = reply,
                fromUser = false
            )
        }
    }

    fun deleteMessage(id: String) {
        _messages.value = _messages.value.filterNot { it.id == id }
    }

    fun clear() {
        _messages.value = emptyList()
        _input.value = ""
    }
}
