// Test commit for chatGPT connection

package com.example.leo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.leo.ui.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen(onFinished = { showSplash = false })
            } else {
                // Your app’s real entry point
                com.example.leo.ui.chat.ChatScreen()
            }
        }
    }
}
