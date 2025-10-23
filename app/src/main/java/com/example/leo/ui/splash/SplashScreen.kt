package com.example.leo.ui.splash

@androidx.compose.runtime.Composable
fun SplashScreen(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.then(
            androidx.compose.foundation.layout.fillMaxSize()
        )
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(
                id = com.example.leo.R.drawable.splash_illustration
            ),
            contentDescription = null,
            modifier = androidx.compose.ui.Modifier.then(
                androidx.compose.foundation.layout.fillMaxSize()
            ),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}