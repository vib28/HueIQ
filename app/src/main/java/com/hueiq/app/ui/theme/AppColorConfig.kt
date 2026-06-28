package com.hueiq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *  HueIQ Color Configuration — SINGLE SOURCE OF TRUTH
 *  Edit hex values here. All theme files, screens and components
 *  reference this object — nothing else should have raw hex literals.
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  Palette design:
 *    Primary   = IBM deep blue   (#0072B2) — safe for ALL colorblind types
 *    Secondary = Amber/gold      (#E69F00) — pairs with blue; safe for all types
 *    Error     = Vermillion      (#D55E00) — readable by red-green colorblind
 *    Avoid pure red or pure green as UI colors — indistinguishable for ~8% of males
 *
 *  Structure:
 *    Palette   → raw named color values  (edit these)
 *    Light     → semantic roles for light mode (references Palette)
 *    Dark      → semantic roles for dark mode  (references Palette)
 *    CtaCard   → "Color Vision Test" hero card (always dark bg, both modes)
 *    Spectrum  → logo iris only (decorative — not brand UI)
 */
object AppColorConfig {

    // ══════════════════════════════════════════════════════════
    //  PALETTE — raw named colors. Edit these hex codes.
    // ══════════════════════════════════════════════════════════
    object Palette {
        // Blues
        val IbmBlue         = Color(0xFF0072B2)   // IBM-safe deep blue
        val SkyBlue         = Color(0xFF56B4E9)   // sky blue (lighter, for dark mode primary)
        val LightSkyBlue    = Color(0xFF88CCEE)   // very light sky (dark mode tertiary)
        val DeepNavy        = Color(0xFF003B6F)   // very dark blue (text on light blue bg)
        val MidNavy         = Color(0xFF004F8C)   // mid dark blue (dark primary container)
        val DarkNavy        = Color(0xFF0A1628)   // almost-black navy (dark bg, CTA card bg)
        val DarkSurface     = Color(0xFF162236)   // dark blue-grey (dark surface)
        val DarkSurfaceVar  = Color(0xFF1E3248)   // dark surface variant

        // Ambers
        val Amber           = Color(0xFFE69F00)   // warm gold (light mode secondary)
        val BrightAmber     = Color(0xFFFFB700)   // brighter gold (dark mode secondary)
        val LightAmber      = Color(0xFFFFF0C2)   // very light amber (secondary container)
        val DarkAmber       = Color(0xFF4A3600)   // dark amber (on secondary container)

        // Neutrals
        val White           = Color(0xFFFFFFFF)
        val NearWhite       = Color(0xFFFAFAFA)   // background
        val LightBlueGrey   = Color(0xFFEEF2F7)   // surface variant (light)
        val SlateGrey       = Color(0xFF4A5568)   // muted text (light mode)
        val NearBlack       = Color(0xFF1C1C1C)   // primary text (light mode)
        val LightGrey       = Color(0xFFE8EDF2)   // primary text (dark mode)
        val MutedLight      = Color(0xFFB0BEC8)   // muted text (dark mode)

        // Error
        val Vermillion      = Color(0xFFD55E00)   // error (light mode) — readable by colorblind
        val LightVermillion = Color(0xFFFF8C42)   // error (dark mode)

        // Light containers
        val LightBlueContainer  = Color(0xFFCCE5F5)
        val LightAmberContainer = Color(0xFFFFF0C2)
        val LightAmberText      = Color(0xFF4A3600)
        val DarkAmberContainer  = Color(0xFFFFE08A)
    }

    // ══════════════════════════════════════════════════════════
    //  LIGHT MODE — semantic color roles
    // ══════════════════════════════════════════════════════════
    object Light {
        val primary              = Palette.IbmBlue
        val onPrimary            = Palette.White
        val primaryContainer     = Palette.LightBlueContainer
        val onPrimaryContainer   = Palette.DeepNavy

        val secondary            = Palette.Amber
        val onSecondary          = Palette.NearBlack
        val secondaryContainer   = Palette.LightAmber
        val onSecondaryContainer = Palette.DarkAmber

        val tertiary             = Palette.SkyBlue
        val onTertiary           = Palette.DeepNavy

        val error                = Palette.Vermillion
        val onError              = Palette.White

        val background           = Palette.NearWhite
        val surface              = Palette.White
        val onBackground         = Palette.NearBlack
        val onSurface            = Palette.NearBlack
        val surfaceVariant       = Palette.LightBlueGrey
        val onSurfaceVariant     = Palette.SlateGrey

        // Feature card backgrounds (light mode tinted)
        val cardScanColor        = Color(0xFFCCE5F5)   // light IBM blue
        val cardColorLibrary     = Color(0xFFFFF0C2)   // light amber
        val cardVisionModes      = Color(0xFFD6EEF8)   // light sky blue
    }

    // ══════════════════════════════════════════════════════════
    //  DARK MODE — semantic color roles
    // ══════════════════════════════════════════════════════════
    object Dark {
        val primary              = Palette.SkyBlue
        val onPrimary            = Palette.DeepNavy
        val primaryContainer     = Palette.MidNavy
        val onPrimaryContainer   = Color(0xFFD0E8FF)

        val secondary            = Palette.BrightAmber
        val onSecondary          = Palette.NearBlack
        val secondaryContainer   = Palette.DarkAmber
        val onSecondaryContainer = Palette.DarkAmberContainer

        val tertiary             = Palette.LightSkyBlue
        val onTertiary           = Palette.DeepNavy

        val error                = Palette.LightVermillion
        val onError              = Palette.NearBlack

        val background           = Palette.DarkNavy
        val surface              = Palette.DarkSurface
        val onBackground         = Palette.LightGrey
        val onSurface            = Palette.LightGrey
        val surfaceVariant       = Palette.DarkSurfaceVar
        val onSurfaceVariant     = Palette.MutedLight
    }

    // ══════════════════════════════════════════════════════════
    //  CTA CARD — "Color Vision Test" hero card
    //  Light: amber (matches Color Library tile)
    //  Dark:  surfaceVariant (matches all other tiles)
    //  Colors here are for the LIGHT mode card.
    //  Dark mode uses MaterialTheme.colorScheme.surfaceVariant (from Dark object).
    // ══════════════════════════════════════════════════════════
    object CtaCard {
        // Light mode — amber card (same family as Color Library)
        val lightBackground = Palette.LightAmber           // #FFF0C2
        val lightText       = Palette.DarkAmber            // #4A3600 — dark amber, readable
        val lightSubtext    = Color(0xFF6B5200)            // slightly lighter dark amber
        val lightIconBg     = Palette.Amber.copy(alpha = 0.2f)
        val lightIcon       = Color(0xFFB87800)            // warm dark amber icon
        val lightButtonBg   = Palette.IbmBlue
        val lightButtonText = Palette.White

        // Dark mode — surfaceVariant (same as all other tiles)
        // Handled in HomeScreen via MaterialTheme.colorScheme — no hardcoded values needed.
    }

    // ══════════════════════════════════════════════════════════
    //  FILLED BUTTONS — explicit colors independent of primary/
    //  onPrimary tokens. In dark mode, primary = SkyBlue and
    //  onPrimary = DeepNavy (dark text on light bg = looks off).
    //  Use ButtonDefaults.buttonColors() with these values for
    //  all filled Button composables.
    // ══════════════════════════════════════════════════════════
    object ButtonFilled {
        val containerColor = Palette.IbmBlue
        val contentColor   = Palette.White
    }

    // ══════════════════════════════════════════════════════════
    //  SPECTRUM — logo iris only (decorative, not brand UI)
    // ══════════════════════════════════════════════════════════
    object Spectrum {
        val Red    = Color(0xFFE53935)
        val Orange = Color(0xFFFF9800)
        val Yellow = Color(0xFFFFEB3B)
        val Green  = Color(0xFF4CAF50)
        val Blue   = Color(0xFF2196F3)
        val Violet = Color(0xFF9C27B0)
    }
}
