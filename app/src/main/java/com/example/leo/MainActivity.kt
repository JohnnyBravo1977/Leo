package com.example.leo

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.leo.ui.LittleGeniusApp
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        splash.setOnExitAnimationListener { provider ->
            val view = provider.view
            val fade = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
            fade.duration = 280L
            fade.doOnEnd { provider.remove() }
            fade.start()
        }

        setContent { LittleGeniusApp() }
    }
}
