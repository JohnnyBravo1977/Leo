
package com.example.leo.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.io.Closeable
import java.util.Locale

class TtsSpeaker(context: Context) : Closeable, TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context, this)
    @Volatile private var ready = false

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) tts.language = Locale.US
    }

    fun speak(text: String) {
        if (!ready) return
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, "utt-${System.currentTimeMillis()}")
    }

    override fun close() { tts.shutdown() }
}