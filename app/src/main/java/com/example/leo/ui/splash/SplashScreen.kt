package com.example.leo.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.leo.R
import kotlinx.coroutines.delay

/**
 * Full-screen splash that shows your illustration and then calls [onFinished].
 * We use ContentScale.Crop so it fills the screen edge-to-edge.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.launch_screen),
        contentDescription = "Splash",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )

    // When your app is ready, call onFinished(). Here we just wait ~1s.
    LaunchedEffect(Unit) {
        delay(5000) // tweak if you want longer/shorter
        onFinished()
    }
}
