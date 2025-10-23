package com.example.leo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.leo.ui.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ✅ These now resolve because of the runtime imports above
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen(onFinished = { showSplash = false })
            } else {
                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    com.example.leo.ui.chat.ChatScreen()   // or whatever your main composable is
                }

            }
        }
    }
}