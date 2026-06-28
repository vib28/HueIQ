package com.hueiq.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueiq.app.ui.auth.AuthViewModel
import com.hueiq.app.ui.auth.SignInState
import com.hueiq.app.ui.components.BlinkMode
import com.hueiq.app.ui.components.HueIQLogo
import com.hueiq.app.ui.theme.AppDarkBackground
import com.hueiq.app.ui.theme.AppPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

/**
 * Splash screen that doubles as the session-restore gate.
 * Waits for AuthViewModel to finish reading DataStore (SignInState.Loading),
 * then routes to Home if a session exists, or Sign In if not.
 * Minimum display time of 1.5s ensures the splash is always seen.
 */
@Composable
fun LoadingScreen(
    viewModel: AuthViewModel,
    onResult: (isLoggedIn: Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        val minDisplayMs = 1500L
        val startTime = System.currentTimeMillis()

        // Wait until the DataStore read is done (state leaves Loading)
        viewModel.signInState
            .filter { it !is SignInState.Loading }
            .first()

        // Respect minimum splash display time
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed < minDisplayMs) delay(minDisplayMs - elapsed)

        onResult(viewModel.signInState.value is SignInState.Success)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val subtitleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtitleAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppPrimary, AppDarkBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HueIQLogo(
                size = 180.dp,
                animScale = scale,
                blinkMode = BlinkMode.CONTINUOUS,
                eyeOnly = true
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "HueIQ",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Know your color vision",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = subtitleAlpha)
            )

            Spacer(modifier = Modifier.height(52.dp))

            CircularProgressIndicator(
                color = Color.White.copy(alpha = 0.7f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
    // Preview can't use a real ViewModel — wrap in a fake that never completes
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppPrimary, AppDarkBackground))),
        contentAlignment = Alignment.Center
    ) {
        Text("HueIQ", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
    }
}
