package com.hueiq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Re-exports from AppColorConfig for backward compatibility.
 * To change colors, edit AppColorConfig.kt — not this file.
 */

// Light mode
val AppPrimary          get() = AppColorConfig.Light.primary
val AppSecondary        get() = AppColorConfig.Light.secondary
val AppTertiary         get() = AppColorConfig.Light.tertiary
val AppError            get() = AppColorConfig.Light.error
val AppBackground       get() = AppColorConfig.Light.background
val AppSurface          get() = AppColorConfig.Light.surface
val AppOnPrimary        get() = AppColorConfig.Light.onPrimary
val AppOnSecondary      get() = AppColorConfig.Light.onSecondary
val AppOnBackground     get() = AppColorConfig.Light.onBackground
val AppOnSurface        get() = AppColorConfig.Light.onSurface
val AppSurfaceVariant   get() = AppColorConfig.Light.surfaceVariant

// Dark mode
val AppDarkBackground   get() = AppColorConfig.Dark.background
val AppDarkSurface      get() = AppColorConfig.Dark.surface
val AppDarkPrimary      get() = AppColorConfig.Dark.primary
val AppDarkSecondary    get() = AppColorConfig.Dark.secondary
val AppDarkTertiary     get() = AppColorConfig.Dark.tertiary
val AppDarkError        get() = AppColorConfig.Dark.error

// Spectrum (logo iris only)
val SpectrumRed    get() = AppColorConfig.Spectrum.Red
val SpectrumOrange get() = AppColorConfig.Spectrum.Orange
val SpectrumYellow get() = AppColorConfig.Spectrum.Yellow
val SpectrumGreen  get() = AppColorConfig.Spectrum.Green
val SpectrumBlue   get() = AppColorConfig.Spectrum.Blue
val SpectrumViolet get() = AppColorConfig.Spectrum.Violet
