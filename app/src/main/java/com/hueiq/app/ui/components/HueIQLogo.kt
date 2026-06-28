package com.hueiq.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hueiq.app.R
import kotlinx.coroutines.delay

/** Controls how the eye blinks. */
enum class BlinkMode {
    NONE,        // static — no animation
    ONCE,        // single blink on first appearance, then stays open
    CONTINUOUS   // repeating blink every ~3 seconds
}

/**
 * The official HueIQ app icon rendered as a composable — uses the same
 * vector drawables as the launcher icon so the logo is always in sync.
 *
 * @param size           Diameter of the clipped circle
 * @param animScale      Optional outer scale (for the loading screen pulse)
 * @param roundedPercent Corner radius as % of size. 50 = full circle
 * @param blinkMode        NONE / ONCE / CONTINUOUS
 * @param blinkIntervalMs  Pause between blinks in CONTINUOUS mode (default 3 s)
 */
@Composable
fun HueIQLogo(
    size: Dp = 96.dp,
    animScale: Float = 1f,
    roundedPercent: Int = 50,
    blinkMode: BlinkMode = BlinkMode.CONTINUOUS,
    blinkIntervalMs: Long = 3_000L,
    eyeOnly: Boolean = false
) {
    // scaleY of the foreground — 1f = open eye, ~0f = closed (blink)
    val blinkScale = remember { Animatable(1f) }

    LaunchedEffect(blinkMode, blinkIntervalMs) {
        when (blinkMode) {
            BlinkMode.NONE -> { /* stay open */ }

            BlinkMode.ONCE -> {
                delay(400L)
                blinkScale.animateTo(0.05f, tween(80))
                delay(60L)
                blinkScale.animateTo(1f, tween(120))
            }

            BlinkMode.CONTINUOUS -> {
                while (true) {
                    delay(blinkIntervalMs)
                    blinkScale.animateTo(0.05f, tween(80))
                    delay(60L)
                    blinkScale.animateTo(1f, tween(120))
                }
            }
        }
    }

    if (eyeOnly) {
        // Standalone eye — no circle background, no clip.
        // Uses ic_eye.xml which has a navy outline so it reads on any background.
        Image(
            painter = painterResource(R.drawable.ic_eye),
            contentDescription = "HueIQ eye",
            modifier = Modifier
                .size(size)
                .scale(animScale)
                .graphicsLayer {
                    scaleY = if (blinkMode != BlinkMode.NONE) blinkScale.value else 1f
                }
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .scale(animScale)
                .clip(RoundedCornerShape(roundedPercent)),
            contentAlignment = Alignment.Center
        ) {
            // Layer 1 — background (always static)
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            // Layer 2 — foreground eye: blink by squishing scaleY
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "HueIQ logo",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(0.7f)
                    .graphicsLayer {
                        scaleY = if (blinkMode != BlinkMode.NONE) blinkScale.value else 1f
                    }
            )
        }
    }
}
