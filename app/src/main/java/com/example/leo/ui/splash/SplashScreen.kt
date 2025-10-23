package com.example.leo.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.leo.R
import kotlinx.coroutines.delay

/**
 * Full-bleed splash artwork.
 * Android 12+ shows the system splash briefly; this composable fades in and holds.
 * Total display time ~7s (1s fade + 6s hold), then onFinished() is invoked.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1s fade in + 6s hold = ~7s total
        alpha.animateTo(1f, animationSpec = tween(1000))
        delay(500)
        onFinished()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash_illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = alpha.value),
            contentScale = ContentScale.Crop
        )
    }
}
