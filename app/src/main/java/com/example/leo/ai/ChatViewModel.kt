package com.example.leo.ai


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUi(
    val history: List<Pair<String,String>> =
        listOf("system" to "You are LittleGenius: warm, truthful, concise."),
    val thinking: Boolean = false,
    val error: String? = null
)

class ChatViewModel(private val client: ChatClient = ChatClient()) : ViewModel() {
    private val _ui = MutableStateFlow(ChatUi())
    val ui: StateFlow<ChatUi> = _ui

    fun send(text: String) {
        val msg = text.trim()
        if (msg.isBlank() || _ui.value.thinking) return
        val newHist = _ui.value.history + ("user" to msg)
        _ui.value = _ui.value.copy(history = newHist, thinking = true, error = null)

        viewModelScope.launch {
            try {
                val reply = client.send(newHist)
                _ui.value = _ui.value.copy(
                    history = _ui.value.history + ("assistant" to reply),
                    thinking = false
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(thinking = false, error = t.message ?: "Unknown error")
            }
        }
    }
}