package com.example.leo.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val fromUser: Boolean,
    val time: String = DateTimeFormatter.ofPattern("h:mm a").format(LocalDateTime.now()),
    val delivered: Boolean = true // simple ✓ badge
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _input = MutableStateFlow("")
    val input = _input.asStateFlow()

    fun onInputChange(value: String) {
        _input.value = value
    }

    fun sendMessage() {
        val text = _input.value.trim()
        if (text.isEmpty()) return

        // add user message
        val updated = _messages.value + ChatMessage(text = text, fromUser = true)
        _messages.value = updated
        _input.value = ""

        // fake bot reply after a beat
        viewModelScope.launch {
            delay(550)
            _messages.value = _messages.value + ChatMessage(
                text = autoReply(text),
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

    private fun autoReply(userText: String): String =
        "You said: \"$userText\" — noted! 👌"
}
