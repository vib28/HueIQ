# HueIQ — Project Walkthrough

> **Living document.** This file must be updated after every implementation task — features, bug fixes, refactors. See also `CLAUDE.md` for the full knowledge base (architecture decisions, dev journey, rules).

A colorblindness estimation app that identifies if a user has color vision deficiency and which type. All data is stored **locally on the device** — no backend required.

---

## App Identity

| Property | Value |
|----------|-------|
| **App Name** | HueIQ |
| **Package ID** | `com.hueiq.app` |
| **Tagline** | Know your color vision |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |

> **TrueHue** was considered but is taken on the Play Store ("TrueHue AI" — a makeup shade app). **HueIQ** has no exact match.

---

## Architecture

```
app/src/main/java/com/hueiq/app/
├── data/
│   ├── UserData.kt           # User profile model (userId, displayName, email, photoUrl)
│   └── UserRepository.kt     # DataStore: user session + theme_mode (SYSTEM/LIGHT/DARK)
├── navigation/
│   └── NavGraph.kt           # Navigation graph; receives AuthViewModel from MainActivity
├── ui/
│   ├── auth/
│   │   └── AuthViewModel.kt  # AndroidViewModel; Google Sign-In, DataStore session, cycleTheme()
│   ├── screens/
│   │   ├── LoadingScreen.kt       # Splash + session-restore gate
│   │   ├── SignInScreen.kt        # Google Sign-In UI; adaptive Google button for dark mode
│   │   ├── HomeScreen.kt          # Main screen; theme toggle + feature cards (all theme-adaptive)
│   │   └── IshiharaTestScreen.kt  # Ishihara test UI shell (logic pending)
│   ├── components/
│   │   └── HueIQLogo.kt      # Animated blinking eye composable (BlinkMode + blinkIntervalMs)
│   └── theme/
│       ├── AppColorConfig.kt # ★ SINGLE SOURCE OF TRUTH for all colors — edit here
│       ├── Color.kt          # Thin re-exports from AppColorConfig (backward compat)
│       ├── Theme.kt          # HueIQTheme; ThemeMode enum; LocalDarkTheme CompositionLocal
│       └── Type.kt           # Typography
└── MainActivity.kt           # Reads themeMode from ViewModel, passes to HueIQTheme
```

---

## Screens

### 1. Loading Screen
- **Animated standalone eye** (180 dp, no circle background) — blinking continuously every 3 s
- Eye drawn from `drawable/ic_eye.xml` — navy-outlined almond + 6-sector rainbow iris + dark pupil
- Pulsing scale animation on the eye while loading
- Waits for `AuthViewModel.init` to finish reading DataStore (`SignInState.Loading` → resolved)
- Enforces 1.5 s minimum display time
- Routes to **Home** if session exists, **Sign In** if not

### 2. Sign In Screen
- **Animated standalone eye** (140 dp, no circle background) — blinks once every **10 s**
- Login / Sign Up toggle (switches button label; hides "Forgot password?" on Sign Up)
- Email + password fields with visibility toggle
- **Sign in with Google** — full-width branded button, two-step Credential Manager flow:
  - Step 1: auto-selects previously used account (zero-friction)
  - Step 2: falls back to full account picker if none found
- Facebook and X login **removed**
- Errors shown via `SnackbarHost`
- On success → saves to DataStore → navigates to Home

### 3. Home Screen
- Gradient initial avatar (Blue → Sky Blue)
- Shows name + Google email (from local DataStore)
- **Prominent "Color Vision Test" card** — adapts colors in dark mode
- Feature cards under "More Tools": Scan Color · Color Library · Vision Modes (all theme-adaptive)
- **Theme toggle icon** in top bar — cycles ☀️ Light → 🌙 Dark → ⚙️ System, saved to DataStore
- Sign Out → clears DataStore → back to Sign In

### 4. Ishihara Test Screen (`IshiharaTestScreen.kt`)
- Back arrow → Home
- Info banner explaining the Ishihara test and how to take it
- Placeholder coloured circle (where the generated plate will be rendered)
- "Plate X of 14" counter
- 4 answer buttons in a 2×2 grid (answers wired to logic — TODO)
- Plate generation and scoring logic pending (see **Ishihara Plate Generation** section below)

### 5. Scan Color Screen (`ScanColorScreen.kt`)
- Live CameraX preview fills the screen
- **21×21 center region** sampled every 120 ms (throttled); YUV_420_888 → RGB via BT.601 matrix
- Reticle overlay: ring + center dot + 4 directional ticks drawn on Compose Canvas
- **Bottom panel** (semi-transparent, dark):
  - Color swatch (56dp rounded), detected name, hex code, copy-to-clipboard icon
  - **"Save to Library"** button — saves color to DataStore My Colors; shows "Saved" (disabled) if hex already saved
  - **"View Details"** button — navigates to Color Detail Screen without saving
- CAMERA permission requested at runtime; rationale card shown if denied
- Color matching: Lab-space nearest-neighbor against `ColorNameDatabase` (~150 CSS/X11 colors)

### 6. Color Library Screen (`ColorLibraryScreen.kt`)
- Searchable, filterable catalog of **300+ named colors** + user-saved "My Colors"
- **Filter chips:** ALL, MY COLORS, WHITES, GRAYS, REDS, PINKS, ORANGES, YELLOWS, GREENS, TEALS, BLUES, PURPLES, BROWNS
- **MY COLORS** chip shows only user-saved colors (from DataStore)
- Per-card CVD simulation row: 3 small swatches (D / P / T abbreviations) showing how each color looks to each colorblind type
- Tap any color card → navigates to Color Detail Screen
- Hex code on each card is tappable to copy

### 7. Color Detail Screen (`ColorDetailScreen.kt`)
- **Fullscreen detail** for any color (built-in or saved)
- Large original color swatch (220dp, full width)
- Name + hex + copy button
- "How colorblind users see this" section with 3 `FilterChip`s (Deuteranopia pre-selected)
- Large simulated swatch (220dp, rounded) — updates live as CVD type changes
- CVD info card with description of the selected deficiency type
- **Bookmark icon** in TopAppBar — toggles save/remove from My Colors
- Entry points: Color Library card tap, or Scanner "View Details" button

---

## Colorblind-Accessible Color Palette

HueIQ uses the **IBM colorblind-safe palette** (blue + amber). This is the most universally accessible pairing — safe for ~99% of colorblind users.

| Type | Affected | Can't distinguish | Safe for them |
|------|----------|-------------------|---------------|
| Deuteranopia | ~6% of males | Red vs Green | Blue ✅ Amber ✅ |
| Protanopia | ~2% of males | Red vs Green | Blue ✅ Amber ✅ |
| Tritanopia | ~0.01% | Blue vs Yellow | Red/Orange ✅ |

> Dynamic color is **disabled** (`dynamicColor = false`) — colorblindness testing requires consistent, predictable colors.

---

## Color Configuration (`AppColorConfig.kt`)

**All colors are defined in a single file: `ui/theme/AppColorConfig.kt`**

To change any color, edit only this file. No other file contains raw hex values.

```
AppColorConfig
├── Palette     ← named raw color values (hex here — edit these)
├── Light       ← semantic roles for light mode (references Palette)
├── Dark        ← semantic roles for dark mode  (references Palette)
├── CtaCard     ← "Color Vision Test" hero card (always dark, both modes)
└── Spectrum    ← logo iris colors (decorative only)
```

### Palette (raw hex values)

| Name | Hex | Usage |
|------|-----|-------|
| `IbmBlue` | `#0072B2` | Primary (light) |
| `SkyBlue` | `#56B4E9` | Primary (dark), tertiary (light) |
| `LightSkyBlue` | `#88CCEE` | Tertiary (dark) |
| `Amber` | `#E69F00` | Secondary (light) |
| `BrightAmber` | `#FFB700` | Secondary (dark) |
| `Vermillion` | `#D55E00` | Error (light) |
| `LightVermillion` | `#FF8C42` | Error (dark) |
| `DarkNavy` | `#0A1628` | Dark background, CTA card bg |
| `NearWhite` | `#FAFAFA` | Light background |

### Light Mode Semantic Roles

| Role | Value |
|------|-------|
| primary | IBM Blue `#0072B2` |
| secondary | Amber `#E69F00` |
| tertiary | Sky Blue `#56B4E9` |
| error | Vermillion `#D55E00` |
| background | `#FAFAFA` |
| cardScanColor | `#CCE5F5` |
| cardColorLibrary | `#FFF0C2` |
| cardVisionModes | `#D6EEF8` |

### Dark Mode Semantic Roles

| Role | Value |
|------|-------|
| primary | Sky Blue `#56B4E9` |
| secondary | Bright Amber `#FFB700` |
| error | Light Vermillion `#FF8C42` |
| background | Dark Navy `#0A1628` |
| surface | `#162236` |

### CTA Card (theme-adaptive)

The "Color Vision Test" CTA card on HomeScreen adapts to match other feature tiles:

**Light mode:**
| Role | Value |
|------|-------|
| background | `#FFF0C2` (Light Amber) — matches Color Library tile |
| text | `#4A3600` (Dark Amber) |
| subtext | Dark Amber 70% opacity |
| iconBg | `#E8D28A` (Amber Tint) |
| icon | `#E69F00` (Amber) |
| buttonBg | `#0072B2` (IBM Blue) |
| buttonText | `#FFFFFF` (White) |

**Dark mode:**
| Role | Value |
|------|-------|
| background | `MaterialTheme.colorScheme.surfaceVariant` — matches all other feature tiles |
| text | `MaterialTheme.colorScheme.onSurface` |
| subtext | `onSurfaceVariant` |
| iconBg | `surface` 50% opacity |
| icon | `tertiary` |
| buttonBg | `#0072B2` (IBM Blue) |
| buttonText | `#FFFFFF` (White) |

> **Bug fix:** CTA card previously had dark navy bg in light mode → invisible dark text. Now uses amber bg in light mode (matches Color Library) and surfaceVariant in dark mode (matches other tiles).

---

## Dark Mode & Theme Toggle

- **Follows system** by default (`ThemeMode.SYSTEM`)
- **In-app toggle** in HomeScreen top bar — tap to cycle: ☀️ Light → 🌙 Dark → ⚙️ System
- Preference saved to DataStore key `theme_mode` — persists across app restarts
- `ThemeMode` enum: `SYSTEM` / `LIGHT` / `DARK` — stored in `UserRepository`
- `LocalDarkTheme` CompositionLocal — any composable can read `LocalDarkTheme.current` without parameter drilling
- All screens use `MaterialTheme.colorScheme.*` tokens — no hardcoded colors remain

### Theme flow
```
DataStore (theme_mode key)
  └─► UserRepository.themeFlow (Flow<ThemeMode>)
        └─► AuthViewModel.themeMode
              ├─► MainActivity → HueIQTheme(themeMode = ...)
              └─► NavGraph → HomeScreen(themeMode = ..., onToggleTheme = { cycleTheme() })
```

---

**Launcher icon** — Spectrum-eye adaptive icon  
- **Background:** Dark navy radial gradient (`#1A2E6B` → `#050D2A`)  
- **Foreground:** White almond eye + 6-sector rainbow iris (R/O/Y/G/B/V) + dark pupil + highlight  
- Files: `drawable/ic_launcher_background.xml`, `drawable/ic_launcher_foreground.xml`

**In-app logo** (`ui/components/HueIQLogo.kt`) — `BlinkMode` enum + `blinkIntervalMs` param  
- **`eyeOnly = false`** (default) — launcher icon style inside a clipped circle (used on Home screen)  
- **`eyeOnly = true`** — standalone eye from `drawable/ic_eye.xml`; navy stroke makes it visible on both dark and light backgrounds  
- `BlinkMode.NONE` — static  
- `BlinkMode.ONCE` — single blink on appearance  
- `BlinkMode.CONTINUOUS` — repeating single blink; interval controlled by `blinkIntervalMs`

---

## Local Data Storage (No Backend)

All user data lives in `DataStore<Preferences>` (`data/user_prefs`):

| Key | Value |
|-----|-------|
| `user_id` | Google account email (unique identifier) |
| `display_name` | Full name from Google |
| `email` | Google email |
| `photo_url` | Profile picture URL (nullable) |
| `theme_mode` | `"LIGHT"` / `"DARK"` / absent (= SYSTEM) |
| `saved_colors` | Pipe+semicolon delimited: `"name\|r\|g\|b;name\|r\|g\|b"` |

No auth tokens stored — device-level Google account handles re-auth.

**Sign-out behavior:** Only session keys are cleared (`user_id`, `display_name`, `email`, `photo_url`). `theme_mode` and `saved_colors` are preserved across sign-out.

---

## Google Sign-In Setup

✅ **Already configured** — OAuth credentials created and wired in.

| Credential | Value |
|-----------|-------|
| Web Client ID | `219118991260-i0dk1ta1tngt7b2ojvmd92jud9d70m6d.apps.googleusercontent.com` |
| Android Client ID | `219118991260-iu2eja4sg0uj4udtmu96066vu3v3srqv.apps.googleusercontent.com` |
| Package | `com.hueiq.app` |
| Debug SHA-1 | `EB:D1:13:7C:C7:0E:69:38:AE:FF:5A:B9:2F:9B:83:7A:9A:59:07:06` |

> For a release build, generate a release SHA-1 with `keytool` and add a second Android Client ID in Google Cloud Console.

---

## Navigation Flow

```
App Start
  └─► LoadingScreen (reads DataStore)
        ├─ session found ──► HomeScreen
        └─ no session   ──► SignInScreen
                               └─ Google Sign-In success
                                     └─► HomeScreen
                                           ├─ "Color Vision Test" ──► IshiharaTestScreen
                                           │                               └─ Back ──► HomeScreen
                                           ├─ "Scan Color" ──────────► ScanColorScreen
                                           │                               ├─ "View Details" ──► ColorDetailScreen
                                           │                               │                         └─ Back ──► ScanColorScreen
                                           │                               └─ Back ──► HomeScreen
                                           ├─ "Color Library" ───────► ColorLibraryScreen
                                           │                               ├─ Tap color ──────► ColorDetailScreen
                                           │                               │                         └─ Back ──► ColorLibraryScreen
                                           │                               └─ Back ──► HomeScreen
                                           └─ Sign Out ──► SignInScreen
```

---

## Build

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n "com.hueiq.app/.MainActivity"
```

---

## TODOs

| Priority | Area | Task |
|----------|------|------|
| High | **Ishihara Test** | Implement plate generation + scoring logic (see **Ishihara Plate Generation** section below). Use LCH color space + Poisson disk sampling. |
| Medium | **Profile photo** | Load `UserData.photoUrl` with Coil in HomeScreen avatar. Currently shows initials gradient. |
| Medium | **Email/password auth** | Wire the existing email+password fields in `SignInScreen` to a real auth provider (Firebase Auth or similar). |
| Low | **Vision Modes screen** | Implement the "Vision Modes" card destination — show the user how their surroundings would look with each CVD type. CVD simulation matrices already exist in `ColorLibraryData.kt`. |
| Low | **Ishihara scoring** | After plate generation: track user answers, determine CVD type + severity, show results. |

**Completed:**
- ✅ Camera color scanner (CameraX, Lab-space matching)
- ✅ Color Library (300+ colors, search, category chips, CVD simulation row)
- ✅ Save scanned colors to "My Colors" (DataStore persistence)
- ✅ Color Detail Screen (fullscreen swatch + CVD type selector + save/unsave)
- ✅ Google Sign-In (Credential Manager API)
- ✅ Theme toggle (SYSTEM / LIGHT / DARK, persisted to DataStore)

---

## Ishihara Plate Generation

> Real Ishihara plates are copyrighted by Kanehara Trading Inc. This section describes how to synthesise equivalent plates algorithmically — fully legal and avoids any IP issues.

### How Ishihara Plates Work

Each plate contains:
- A **figure** (digit or shape) hidden in dots of one colour family
- A **background** of dots in a contrasting colour family
- Dots of **randomised size and position** so the pattern cannot be detected by luminance/brightness alone — only by hue

**Key constraint:** colour-blind and normal-vision users must perceive equal luminance across all dots. The figure is encoded purely in hue/chroma, not brightness.

### Plate Types to Include

| Type | Description |
|------|-------------|
| Transformation | Normal vision reads one number; deuteranopes read a different one |
| Vanishing | Only visible to normal vision |
| Hidden digit | Only visible to colour-blind users |
| Diagnostic | Distinguishes protanopia from deuteranopia |

---

### Step 1 — Colour Model: LCH

Use **LCH** (Lightness, Chroma, Hue) — a perceptually uniform colour space.

- **L** (lightness) is held constant across figure and background dots → ensures equal perceived brightness
- **C** (chroma) and **H** (hue) encode the colour difference that only normal vision detects

> ⚠️ Do NOT use RGB, HSV, or HSL — they have non-uniform lightness and will cause the figure to "pop" by brightness for colour-blind users.

**Good LCH pairs for red-green deficiency plates:**

| Role | L | C | H | Appearance (normal) |
|------|---|---|---|---------------------|
| Figure dots | 65 | 55 | 30° | Warm orange-red |
| Background dots | 65 | 55 | 140° | Cool green |

Add ±5–10 noise to L, C, and H per dot for naturalism.

---

### Step 2 — LCH ↔ RGB Conversion (Kotlin)

No Android built-in exists — implement the full pipeline: RGB → linear → XYZ (D65) → Lab → LCH.

```kotlin
fun rgbToLCH(color: Int): Triple<Double, Double, Double> {
    var r = Color.red(color) / 255.0
    var g = Color.green(color) / 255.0
    var b = Color.blue(color) / 255.0

    // Linearise (gamma removal)
    r = if (r > 0.04045) ((r + 0.055) / 1.055).pow(2.4) else r / 12.92
    g = if (g > 0.04045) ((g + 0.055) / 1.055).pow(2.4) else g / 12.92
    b = if (b > 0.04045) ((b + 0.055) / 1.055).pow(2.4) else b / 12.92

    // XYZ (D65 illuminant)
    val x = r * 0.4124 + g * 0.3576 + b * 0.1805
    val y = r * 0.2126 + g * 0.7152 + b * 0.0722
    val z = r * 0.0193 + g * 0.1192 + b * 0.9505

    // Lab
    fun f(t: Double) = if (t > 0.008856) t.pow(1.0 / 3) else 7.787 * t + 16.0 / 116
    val L = 116 * f(y / 1.0) - 16
    val a = 500 * (f(x / 0.9505) - f(y / 1.0))
    val bLab = 200 * (f(y / 1.0) - f(z / 1.089))

    val C = sqrt(a * a + bLab * bLab)
    val H = Math.toDegrees(atan2(bLab, a)).let { if (it < 0) it + 360 else it }
    return Triple(L, C, H)
}
```

---

### Step 3 — Dot Placement: Poisson Disk Sampling

Use Poisson disk sampling to generate irregular, non-overlapping dot centres (matches real plates).

```kotlin
fun poissonDisk(width: Int, height: Int, minDist: Float, maxDots: Int): List<PointF> {
    val active = mutableListOf<PointF>()
    val result = mutableListOf<PointF>()
    val grid = HashMap<Pair<Int, Int>, PointF>()
    val cellSize = minDist / sqrt(2f)

    val first = PointF(width / 2f, height / 2f)
    active.add(first); result.add(first)

    while (active.isNotEmpty() && result.size < maxDots) {
        val origin = active.random()
        var placed = false
        repeat(30) {
            val angle = Math.random() * 2 * PI
            val dist = minDist + Math.random() * minDist
            val candidate = PointF(
                origin.x + (cos(angle) * dist).toFloat(),
                origin.y + (sin(angle) * dist).toFloat()
            )
            if (isValid(candidate, grid, cellSize, minDist, width, height)) {
                active.add(candidate); result.add(candidate)
                grid[gridKey(candidate, cellSize)] = candidate
                placed = true
            }
        }
        if (!placed) active.remove(origin)
    }
    return result
}
```

---

### Step 4 — Assign Dot Colours from Figure Mask

Define the figure digit as a **bitmap mask** (white = figure region, black = background). Assign colours per dot based on which region its centre falls in.

```kotlin
fun assignDotColor(dotCenter: PointF, figureMask: Bitmap,
                   figureColor: Int, bgColor: Int): Int {
    val px = figureMask.getPixel(dotCenter.x.toInt(), dotCenter.y.toInt())
    val isFigure = Color.red(px) > 128
    return if (isFigure) addColorNoise(figureColor) else addColorNoise(bgColor)
}

fun addColorNoise(baseColor: Int): Int {
    val (l, c, h) = rgbToLCH(baseColor)
    return lchToRgb(
        l + (Math.random() * 10 - 5),
        c + (Math.random() * 8 - 4),
        h + (Math.random() * 10 - 5)
    )
}
```

---

### Step 5 — Generate the Figure Mask

Render the digit to a `Bitmap` using Android `Canvas` + `Paint`. This is used as the mask in Step 4.

```kotlin
fun makeTextMask(digit: String, size: Int): Bitmap {
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint().apply {
        color = Color.WHITE
        textSize = size * 0.55f
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawColor(Color.BLACK)
    canvas.drawText(digit, size / 2f, size * 0.65f, paint)
    return bmp
}
```

---

### Step 6 — Vary Dot Sizes

Each dot should have a slightly different radius to prevent pattern detection by size alone.

```
radius = minDist × random(0.35f .. 0.65f)
```

---

### Step 7 — Render in Compose

Draw all dots onto a Compose `Canvas`:

```kotlin
Canvas(modifier = Modifier.size(plateSize)) {
    dots.forEach { (center, radius, color) ->
        drawCircle(
            color = Color(color),
            radius = radius,
            center = center
        )
    }
}
```

---

### Plate Set (14 plates)

Classic Ishihara set numbers to include: **12, 8, 6, 29, 57, 5, 3, 15, 74, 2, 45, hidden, transformation, diagnostic**

Store plate definitions as data objects:

```kotlin
data class IsihiharaPlate(
    val id: Int,
    val digit: String,           // what normal vision sees
    val altDigit: String?,       // what red-green blind sees (null = vanishing plate)
    val figureHue: Float,        // LCH hue for figure dots
    val backgroundHue: Float,    // LCH hue for background dots
    val lightness: Float = 65f,
    val chroma: Float = 55f
)
```

---

### Important Caveats

- **Screen calibration** — results are affected by display colour profiles. Use `ColorSpace` APIs (Android 8+) and inform users.
- **Ambient lighting** — recommend consistent indoor lighting during the test.
- **Disclaimer** — this is a screening tool, not a clinical diagnosis. Direct users to an optometrist for formal assessment.
- **Reproducibility** — seed the random number generator per plate so the same plate always looks identical across sessions.

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2026.02.01 | UI toolkit |
| Navigation Compose | 2.9.8 | Screen routing |
| Credentials | 1.6.0 | Google Sign-In (Credential Manager) |
| Google Identity | 1.2.0 | `GetGoogleIdOption`, ID token parsing |
| DataStore Preferences | 1.1.1 | Local user data persistence |
| Material3 | (via BOM) | Design system |
| Lifecycle ViewModel | 2.6.1 | `AndroidViewModel`, `viewModelScope` |

