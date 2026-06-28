package com.hueiq.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** App-level theme mode — stored in DataStore and read by MainActivity. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * CompositionLocal so any composable can call `LocalDarkTheme.current`
 * to check whether dark mode is active, without parameter drilling.
 */
val LocalDarkTheme = compositionLocalOf { false }

// Color schemes are built entirely from AppColorConfig — no raw hex literals here.
private val DarkColorScheme = darkColorScheme(
    primary              = AppColorConfig.Dark.primary,
    onPrimary            = AppColorConfig.Dark.onPrimary,
    primaryContainer     = AppColorConfig.Dark.primaryContainer,
    onPrimaryContainer   = AppColorConfig.Dark.onPrimaryContainer,
    secondary            = AppColorConfig.Dark.secondary,
    onSecondary          = AppColorConfig.Dark.onSecondary,
    secondaryContainer   = AppColorConfig.Dark.secondaryContainer,
    onSecondaryContainer = AppColorConfig.Dark.onSecondaryContainer,
    tertiary             = AppColorConfig.Dark.tertiary,
    onTertiary           = AppColorConfig.Dark.onTertiary,
    error                = AppColorConfig.Dark.error,
    onError              = AppColorConfig.Dark.onError,
    background           = AppColorConfig.Dark.background,
    surface              = AppColorConfig.Dark.surface,
    onBackground         = AppColorConfig.Dark.onBackground,
    onSurface            = AppColorConfig.Dark.onSurface,
    surfaceVariant       = AppColorConfig.Dark.surfaceVariant,
    onSurfaceVariant     = AppColorConfig.Dark.onSurfaceVariant,
    inverseSurface       = AppColorConfig.Light.onBackground,
    inverseOnSurface     = AppColorConfig.Dark.background
)

private val LightColorScheme = lightColorScheme(
    primary              = AppColorConfig.Light.primary,
    onPrimary            = AppColorConfig.Light.onPrimary,
    primaryContainer     = AppColorConfig.Light.primaryContainer,
    onPrimaryContainer   = AppColorConfig.Light.onPrimaryContainer,
    secondary            = AppColorConfig.Light.secondary,
    onSecondary          = AppColorConfig.Light.onSecondary,
    secondaryContainer   = AppColorConfig.Light.secondaryContainer,
    onSecondaryContainer = AppColorConfig.Light.onSecondaryContainer,
    tertiary             = AppColorConfig.Light.tertiary,
    onTertiary           = AppColorConfig.Light.onTertiary,
    error                = AppColorConfig.Light.error,
    onError              = AppColorConfig.Light.onError,
    background           = AppColorConfig.Light.background,
    surface              = AppColorConfig.Light.surface,
    onBackground         = AppColorConfig.Light.onBackground,
    onSurface            = AppColorConfig.Light.onSurface,
    surfaceVariant       = AppColorConfig.Light.surfaceVariant,
    onSurfaceVariant     = AppColorConfig.Light.onSurfaceVariant,
    inverseSurface       = AppColorConfig.Dark.background,
    inverseOnSurface     = AppColorConfig.Light.onBackground
)

@Composable
fun HueIQTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,       // kept off — brand colors always enforced
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else   -> LightColorScheme
    }

    CompositionLocalProvider(LocalDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

