package com.example.leo

// -----------------------------
// Android / Splash
// -----------------------------
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope

// -----------------------------
// Coroutines
// -----------------------------
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -----------------------------
// App UI entry
// -----------------------------
import com.example.leo.ui.LittleGeniusApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1) Install Android 12+ SplashScreen
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // 2) Keep splash briefly while app initializes
        var keepSplash = true
        lifecycleScope.launch {
            delay(450) // Adjust: shorter = snappier, longer = smoother
            keepSplash = false
        }
        splash.setKeepOnScreenCondition { keepSplash }

        // 3) Fade animation as splash exits
        splash.setOnExitAnimationListener { splashViewProvider ->
            val view = splashViewProvider.view
            val fade = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
            fade.duration = 300L
            fade.doOnEnd { splashViewProvider.remove() }
            fade.start()
        }

        // 4) Launch the NavHost-based app (fix: do NOT call ChatScreen directly)
        setContent {
            LittleGeniusApp()
        }
    }
}
