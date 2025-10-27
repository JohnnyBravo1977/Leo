package com.example.leo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.leo.ui.LittleGeniusApp

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ splashscreen API
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            LeoRoot()
        }
    }
}

@Composable
fun LeoRoot() {
    LittleGeniusApp()
}
