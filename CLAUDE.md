# CLAUDE.md — HueIQ Knowledge Base

This file is the authoritative knowledge base for the HueIQ project. Read it at the start of every session. It documents architecture, every feature, the reasoning behind each decision, and rules that must be followed when adding new code.

---

## 1. Project Identity

| Property | Value |
|----------|-------|
| **App Name** | HueIQ |
| **Tagline** | Know your color vision |
| **Package** | `com.hueiq.app` |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |
| **Compile SDK** | 36.1 |
| **Version** | 1.0 (versionCode 1) |
| **Language** | Kotlin 2.2.10 |
| **UI** | Jetpack Compose — no XML layouts |

**Naming decision:** "TrueHue" was considered first but is taken on the Play Store by a makeup shade matcher called "TrueHue AI". "HueIQ" had no exact match at the time of creation.

**Purpose:** HueIQ detects and raises awareness of color vision deficiency (CVD). It tells users whether they may be colorblind and which type. All data lives locally — no backend, no cloud sync.

---

## 2. Architecture

**Pattern:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow (UDF).

```
DataStore (user_prefs)
     ↓
UserRepository          ← single source of truth for persisted state
     ↓
AuthViewModel           ← owns session + theme state
     ↓
MainActivity            ← reads themeMode, applies HueIQTheme
     ↓
NavGraph                ← receives AuthViewModel, distributes to screens
     ↓
Screens (Composables)   ← receive state + callbacks, no business logic
```

### Full Directory Tree

```
app/src/main/java/com/hueiq/app/
├── MainActivity.kt                         # Entry point; theme application
├── data/
│   ├── UserData.kt                         # User profile model
│   ├── UserRepository.kt                   # DataStore read/write; userFlow + themeFlow
│   ├── ColorLibraryData.kt                 # 190+ named colors + CVD simulation matrices
│   └── ColorNameDatabase.kt                # Lab-distance color lookup for camera
├── navigation/
│   └── NavGraph.kt                         # 6-screen nav graph; sealed class Screen
├── ui/
│   ├── auth/
│   │   └── AuthViewModel.kt                # Google Sign-In, session, cycleTheme()
│   ├── camera/
│   │   ├── ColorAnalyzer.kt                # CameraX ImageAnalysis.Analyzer
│   │   └── ScanColorViewModel.kt           # Detected color state
│   ├── components/
│   │   └── HueIQLogo.kt                    # Reusable animated eye composable
│   ├── screens/
│   │   ├── LoadingScreen.kt                # Splash + async session gate
│   │   ├── SignInScreen.kt                 # Google Sign-In UI
│   │   ├── HomeScreen.kt                   # Main dashboard
│   │   ├── IshiharaTestScreen.kt           # Color vision test (UI shell)
│   │   ├── ScanColorScreen.kt              # Live camera color detection
│   │   ├── ColorLibraryScreen.kt           # Searchable color catalog
│   │   └── ColorDetailScreen.kt            # Fullscreen color detail + CVD selector
│   └── theme/
│       ├── AppColorConfig.kt               # ★ SINGLE SOURCE OF TRUTH for all colors
│       ├── Theme.kt                        # HueIQTheme; ThemeMode; LocalDarkTheme
│       ├── Color.kt                        # Re-exports from AppColorConfig (backward compat)
│       └── Type.kt                         # Material3 Typography
app/src/main/res/
├── drawable/
│   ├── ic_launcher_background.xml          # Dark navy radial gradient
│   ├── ic_launcher_foreground.xml          # 6-sector spectrum eye (launcher icon)
│   ├── ic_eye.xml                          # Standalone eye (navy stroke, transparent bg)
│   ├── ic_google_logo.xml                  # Google sign-in button logo
│   ├── ic_facebook_logo.xml                # Placeholder (not used)
│   └── ic_x_logo.xml                       # Placeholder (not used)
├── mipmap-*/                               # Adaptive icon densities (hdpi → xxxhdpi)
└── values/
    ├── strings.xml                         # Only "HueIQ" — all other strings in-code
    ├── colors.xml                          # Legacy placeholder — NOT the source of truth
    └── themes.xml                          # Parent: Material.Light.NoActionBar
app/src/main/AndroidManifest.xml
```

---

## 3. Development Journey

This section documents the "why" behind every major decision. Read this before proposing changes.

### 3a. Color Palette Design

**Decision: IBM colorblind-safe blue + amber.**

The app is about colorblindness — it cannot afford to use colors that colorblind users can't distinguish. The IBM colorblind-safe palette was chosen because:

| CVD Type | Affected population | Can't distinguish | IBM Blue safe? | IBM Amber safe? |
|----------|--------------------|--------------------|----------------|-----------------|
| Deuteranopia | ~6% of males | Red vs Green | Yes | Yes |
| Protanopia | ~2% of males | Red vs Green | Yes | Yes |
| Tritanopia | ~0.01% | Blue vs Yellow | No | Yes |

Blue + Amber covers ~99% of colorblind users. Pure red and pure green are never used as UI signals.

**Decision: Dynamic color disabled (`dynamicColor = false`).**

Android 12+ dynamic color would recolor the app based on the user's wallpaper. For a colorblindness test, the plate colors must be exactly specified. Dynamic color cannot be allowed to alter any hue.

**Decision: Single source of truth in `AppColorConfig.kt`.**

All hex values live in one object. Screens only reference `MaterialTheme.colorScheme.*` tokens. This makes accessibility audits trivial — one file to check, one file to change.

**Bug history — CTA card:** The "Color Vision Test" hero card originally had a dark navy background in light mode, making dark text invisible. Fixed by switching to an amber background (`#FFF0C2`) in light mode to match the Color Library tile, and `surfaceVariant` in dark mode to match all other feature tiles.

**Key files:** `ui/theme/AppColorConfig.kt`, `ui/theme/Theme.kt`

---

### 3b. Authentication

**Decision: Credential Manager API, not the deprecated Google Play Services sign-in.**

The old `GoogleSignIn` / `GoogleApiClient` API is deprecated. The modern replacement is `androidx.credentials:credentials` + `com.google.android.libraries.identity.googleid`. It uses the OS-level Credential Manager and works the same way passkeys do — the credential selection UI is owned by the OS, not the app.

**Decision: Two-step sign-in flow.**

Step 1: Filter by previously-used Google accounts on this device (`filterByAuthorizedAccounts = true`). If the user has signed into HueIQ before, this auto-selects with zero friction.
Step 2: If no authorized account is found, fall back to the full account picker (`filterByAuthorizedAccounts = false`). This handles first-time sign-in.

**Decision: No backend, no stored tokens.**

The app does not call any server. Google's device-level credential layer handles re-authentication silently. The only thing saved to DataStore is the user's display name, email, and photo URL — profile display data, not auth tokens.

**Current state of email/password fields:** `SignInScreen.kt` has email + password text fields and a Login/Signup toggle. These are UI-only — no backend is wired. The fields exist so the UI looks complete while Google Sign-In is the working auth path.

**OAuth configuration (already set up — do not change these):**
| Credential | Value |
|-----------|-------|
| Web Client ID | `219118991260-i0dk1ta1tngt7b2ojvmd92jud9d70m6d.apps.googleusercontent.com` |
| Android Client ID | `219118991260-iu2eja4sg0uj4udtmu96066vu3v3srqv.apps.googleusercontent.com` |
| Package | `com.hueiq.app` |
| Debug SHA-1 | `EB:D1:13:7C:C7:0E:69:38:AE:FF:5A:B9:2F:9B:83:7A:9A:59:07:06` |

**Key files:** `ui/auth/AuthViewModel.kt`, `ui/screens/SignInScreen.kt`

---

### 3c. Theme System

**Decision: `ThemeMode` enum (SYSTEM / LIGHT / DARK) instead of a boolean.**

A boolean `isDark` only supports two explicit states. Three states are needed: follow the system, force light, force dark. The enum maps cleanly and the cycle direction (Light → Dark → System) was chosen to match common convention.

**Decision: `LocalDarkTheme` CompositionLocal.**

The theme state needs to be readable by any composable (e.g., to swap icon tints or colors). Passing `isDark: Boolean` as a parameter through every composable in the tree would be prop-drilling. `LocalDarkTheme` is provided once at `HueIQTheme` level and read anywhere with `LocalDarkTheme.current`.

**Decision: Persist theme to DataStore.**

The theme preference outlives the process. Key: `theme_mode`, values: `"LIGHT"`, `"DARK"`, or absent (= SYSTEM). Managed by `UserRepository.cycleTheme()` which is called by `AuthViewModel.cycleTheme()` which is called by `HomeScreen`'s toggle button.

**Rule:** All screens must reference `MaterialTheme.colorScheme.*` tokens — never hardcode hex values in screen files.

**Key files:** `ui/theme/Theme.kt`, `ui/theme/AppColorConfig.kt`, `data/UserRepository.kt`

---

### 3d. Navigation & Session Gate

**Decision: LoadingScreen as the async gate.**

On cold start, the app needs to read DataStore to decide whether to send the user to Home or SignIn. Reading DataStore is async. Without a gate, the user sees a flash of SignIn before being redirected to Home (or vice versa). LoadingScreen holds the user in a splash state for at least 1.5 seconds AND until `AuthViewModel` finishes reading DataStore — whichever is longer.

**Decision: AuthViewModel is constructed in MainActivity, not per-screen.**

The ViewModel is created once with `viewModels()` in `MainActivity` and passed down to `NavGraph`, which distributes it to screens. This ensures the same ViewModel instance is shared across navigation — no duplicate session reads, no re-auth on screen transitions.

**Navigation routes (sealed class `Screen`):**

| Route | Screen |
|-------|--------|
| `loading` | LoadingScreen |
| `signin` | SignInScreen |
| `home` | HomeScreen |
| `ishihara_test` | IshiharaTestScreen |
| `scan_color` | ScanColorScreen |
| `color_library` | ColorLibraryScreen |
| `color_detail/{r}/{g}/{b}/{name}` | ColorDetailScreen |

`Screen.ColorDetail.createRoute(r, g, b, name)` URL-encodes the name via `Uri.encode()`.

**Navigation flow:**
```
App Start → Loading (1.5s min + DataStore read)
  ├─ Session exists → Home
  └─ No session → SignIn → Home (on success)
                    Home → IshiharaTest → Home (back)
                    Home → ScanColor ──→ ColorDetail (back to ScanColor)
                         └─ Home (back)
                    Home → ColorLibrary → ColorDetail (back to ColorLibrary)
                         └─ Home (back)
                    Home → Sign Out → SignIn
```

**Key files:** `navigation/NavGraph.kt`, `ui/screens/LoadingScreen.kt`, `MainActivity.kt`

---

### 3e. Camera Color Detection

**Decision: CameraX over Camera2 directly.**

CameraX is a Jetpack library that wraps Camera2 with lifecycle awareness. Binding the camera to the `lifecycleOwner` means it automatically starts and stops with the screen — no manual cleanup needed. Camera2 directly would require managing the camera state machine manually.

**Decision: Sample a 21×21 center region, not the full frame.**

Analyzing every pixel of a full camera frame (e.g., 1920×1080 = 2M pixels) would be too slow for real-time. A 21×21 center square (441 pixels) gives a stable color reading from whatever is centered in the reticle. The center corresponds to where the reticle ring is drawn.

**Decision: YUV_420_888 → RGB conversion, not requesting RGBA directly.**

CameraX `ImageAnalysis` delivers frames in YUV_420_888 because it's the native camera format — converting to RGBA in the hardware pipeline would add latency. The analyzer does the YUV→RGB conversion in software using the standard BT.601 matrix.

**Decision: 120ms throttle.**

Real-time color sampling at 30 FPS would update the UI 30 times per second — imperceptible to the user and wasteful. 120ms gives ~8 effective updates per second, which feels live without taxing the CPU.

**Decision: Lab color space for nearest-color matching.**

Euclidean distance in RGB space is perceptually non-uniform — a small RGB delta in one region of color space looks like a larger change than the same delta elsewhere. CIE Lab is designed to be perceptually uniform: equal distances look equally different to the human eye. The `ColorNameDatabase` converts the sampled RGB to Lab, then finds the nearest named color by Lab distance.

**Key files:** `ui/camera/ColorAnalyzer.kt`, `ui/camera/ScanColorViewModel.kt`, `ui/screens/ScanColorScreen.kt`, `data/ColorNameDatabase.kt`

---

### 3f. Color Library

**Decision: Two separate color databases.**

`ColorNameDatabase.kt` (~150 CSS + X11 named colors) is used exclusively by the camera scanner for real-time nearest-name lookup via Lab distance. It is optimized for speed.

`ColorLibraryData.kt` (190+ colors, 12 categories) is the structured library that users browse. It carries category metadata, is searchable by name, and includes CVD simulation output per color. These are different use cases — merging them would complicate both.

**CVD simulation matrices (in `ColorLibraryData.kt`):**

The simulation applies standard transformation matrices in linear sRGB space to approximate what a colorblind person sees. Three types supported:

- **Deuteranopia** — green receptor deficiency (~6% of males)
- **Protanopia** — red receptor deficiency (~2% of males)
- **Tritanopia** — blue receptor deficiency (~0.01%)

**Color categories:** ALL, MY_COLORS, WHITES, GRAYS, REDS, PINKS, ORANGES, YELLOWS, GREENS, TEALS, BLUES, PURPLES, BROWNS.

`MY_COLORS` is a dynamic category that shows only colors the user has saved from the camera scanner. It integrates into the existing `FilterChip` row automatically because it is an enum value.

**Color count:** ~300+ entries after the color expansion (added ~75–80 named colors across all categories).

**Key files:** `data/ColorLibraryData.kt`, `data/ColorNameDatabase.kt`, `ui/screens/ColorLibraryScreen.kt`, `ui/screens/ColorDetailScreen.kt`

---

### 3g. HueIQLogo Component

**Decision: `BlinkMode` enum instead of a boolean.**

The logo is used on three screens with different blink behavior:
- LoadingScreen: blinks continuously every 3s (+ outer pulse animation)
- SignInScreen: blinks once every 10s (subtle, not distracting)
- HomeScreen: launcher-icon style (no blink needed)

A single `isBlink: Boolean` couldn't express the interval. `BlinkMode` enum (NONE / ONCE / CONTINUOUS) + `blinkIntervalMs` parameter handles all cases cleanly.

**Decision: `eyeOnly` parameter.**

The logo has two visual modes:
- `eyeOnly = false`: The launcher icon style — background layer (dark navy radial gradient) + foreground layer (spectrum eye), clipped to a circle. Used on HomeScreen as a branded avatar.
- `eyeOnly = true`: Just the eye SVG from `ic_eye.xml` on a transparent background, with a navy stroke that makes it visible on both dark and light backgrounds. Used on LoadingScreen and SignInScreen.

**Key files:** `ui/components/HueIQLogo.kt`, `res/drawable/ic_eye.xml`, `res/drawable/ic_launcher_foreground.xml`

---

### 3i. Saved Colors (My Colors)

**Decision: DataStore delimited-string encoding for saved colors.**

Rather than adding Room (a full SQL dependency) just for a user color list, colors are persisted as a pipe+semicolon delimited string in the existing `user_prefs` DataStore: `"name|r|g|b;name|r|g|b"`. This avoids a new dependency and keeps all persistence in one place.

**Decision: Preserve saved colors across sign-out.**

`clearUser()` only removes user session keys (`user_id`, `display_name`, `email`, `photo_url`). It does NOT remove `saved_colors` or `theme_mode`. Rationale: the color library is personal data the user built — losing it on sign-out would be surprising and frustrating.

**Decision: Duplicate detection by hex.**

`saveColor()` checks if the incoming color's hex already exists in the list. This prevents double-saving the same color from the scanner.

**Decision: `ColorCategory.MY_COLORS` instead of a separate list.**

Rather than a parallel "saved list" UI, saved colors surface naturally as a filter chip in the existing Color Library category row. When `MY_COLORS` is selected, the library shows only user-saved entries. When `ALL` is selected, saved entries are merged with the built-in list.

**Decision: `isSaved` state in `ScanColorScreen` to toggle button label.**

The scan panel compares the current detected color's hex against `savedColors` list from `AuthViewModel`. If already saved, the "Save to Library" button shows "Saved" and is disabled — preventing duplicate saves without requiring a toast.

**Key files:** `data/UserRepository.kt`, `data/UserData.kt` (SavedColor), `ui/auth/AuthViewModel.kt`, `ui/screens/ScanColorScreen.kt`, `ui/screens/ColorLibraryScreen.kt`, `navigation/NavGraph.kt`

---

### 3j. Color Detail Screen

**Decision: Fullscreen color detail reachable from both scanner and library.**

Tapping any color in ColorLibraryScreen, or tapping "View Details" in ScanColorScreen, navigates to `ColorDetailScreen`. This is a new route `color_detail/{r}/{g}/{b}/{name}` — RGB are passed as integers; name is URL-encoded via `Uri.encode()` to handle spaces and special characters safely.

**Decision: Save/unsave toggle in the TopAppBar.**

The bookmark icon in the top bar lets the user save or remove a color without leaving the detail screen. `isSaved` is computed in `NavGraph` by comparing the color's hex against `authViewModel.savedColors`. This keeps state in the ViewModel, not in the composable.

**Key file:** `ui/screens/ColorDetailScreen.kt`

---

### 3h. Ishihara Test

**Current state:** UI shell only. The screen shows a placeholder circle, a "Plate X of 14" counter, and 4 answer buttons. No plate generation or scoring logic is implemented.

**Planned implementation decisions (not yet built):**

**Why LCH, not RGB/HSV/HSL for plate generation:**
Real Ishihara plates work by encoding the figure in hue/chroma while holding lightness constant across figure and background dots. If lightness varies, colorblind users can detect the figure by brightness alone — defeating the test. RGB, HSV, and HSL all have non-uniform lightness across hue. LCH (Lightness, Chroma, Hue) is perceptually uniform in lightness — holding L constant in LCH actually holds perceived brightness constant.

**Why Poisson disk sampling for dot placement:**
Dots in real Ishihara plates are irregular and non-overlapping. A regular grid would create detectable patterns (spatial frequency artifacts) that allow pattern recognition without color. Poisson disk sampling generates the irregular, naturally-spaced distribution that matches authentic plates.

**Plate types to implement (14 total):**
| Type | Description |
|------|-------------|
| Transformation | Normal vision reads one number; deuteranopes/protanopes read a different one |
| Vanishing | Only visible to normal vision |
| Hidden digit | Only visible to colorblind users |
| Diagnostic | Distinguishes protanopia from deuteranopia |

**Full algorithmic spec** (LCH↔RGB conversion, Poisson disk sampling, figure mask generation, dot coloring) is in `walkthrough.md` §Ishihara Plate Generation. Do not re-derive it — use that spec.

**Key file:** `ui/screens/IshiharaTestScreen.kt`

---

## 4. Screen Reference

### LoadingScreen (`ui/screens/LoadingScreen.kt`)
- **Purpose:** Splash + async session restore gate. Prevents auth-state flicker.
- **Logo:** `HueIQLogo(eyeOnly=true, size=180.dp, blinkMode=CONTINUOUS, blinkIntervalMs=3000, animScale=<pulse>)`
- **Logic:** Waits for both (a) 1.5s minimum elapsed and (b) `AuthViewModel.signInState != Loading`
- **Navigation out:** → Home (session exists) or → SignIn (no session)

### SignInScreen (`ui/screens/SignInScreen.kt`)
- **Purpose:** Google Sign-In UI; email+password fields (UI-only, not wired)
- **Logo:** `HueIQLogo(eyeOnly=true, size=140.dp, blinkMode=CONTINUOUS, blinkIntervalMs=10000)`
- **Auth:** Calls `AuthViewModel.signInWithGoogle(context)` on button tap
- **Error display:** `SnackbarHost`
- **Navigation out:** → Home (on success)

### HomeScreen (`ui/screens/HomeScreen.kt`)
- **Purpose:** Main dashboard; entry point to all features
- **User display:** Gradient avatar (initials), name + email from DataStore
- **Cards:** "Color Vision Test" (CTA card, amber bg), Scan Color, Color Library, Vision Modes
- **Theme toggle:** Top bar icon, cycles Light→Dark→System, persists to DataStore
- **Sign-out:** Clears DataStore → navigates to SignIn
- **Navigation out:** → IshiharaTest, → ScanColor, → ColorLibrary

### IshiharaTestScreen (`ui/screens/IshiharaTestScreen.kt`)
- **Purpose:** Color vision test — UI shell, logic pending
- **TODO:** Implement plate generation (see §3h and `walkthrough.md`)
- **Navigation out:** ← back to Home

### ScanColorScreen (`ui/screens/ScanColorScreen.kt`)
- **Purpose:** Real-time camera color identification
- **Camera:** CameraX Preview + ImageAnalysis bound to `LocalLifecycleOwner`
- **Overlay:** Reticle ring + center dot + directional ticks drawn on Canvas
- **Panel:** Color swatch + name + hex + copy button + "Save to Library" button + "View Details" button
- **Save behavior:** Button shows "Saved" (disabled) if current hex already in `savedColors`; otherwise saves and shows Toast "Saved to My Colors"
- **Permission:** Requests CAMERA at runtime; shows rationale dialog if denied
- **Parameters:** `savedColors`, `onSaveColor(r,g,b,name)`, `onViewDetails(r,g,b,name)`
- **Navigation out:** ← back to Home, → ColorDetail (View Details)

### ColorLibraryScreen (`ui/screens/ColorLibraryScreen.kt`)
- **Purpose:** Browsable, searchable reference of 300+ named colors + user-saved colors
- **Features:** Search field, category filter chips (including MY_COLORS), per-color CVD simulation row (D/P/T swatches), tap any card → ColorDetail
- **Parameters:** `savedColors: List<SavedColor>`, `onColorClick(r,g,b,name)`
- **MY_COLORS filter:** Shows only user-saved colors; ALL filter merges built-in + saved
- **Navigation out:** ← back to Home, → ColorDetail (card tap)

### ColorDetailScreen (`ui/screens/ColorDetailScreen.kt`)
- **Purpose:** Fullscreen color detail with CVD simulation selector
- **Layout:** TopAppBar (back + bookmark toggle), large original swatch (220dp), name+hex+copy row, "How colorblind users see this" section, 3 FilterChips (Deuteranopia pre-selected), large simulated swatch (220dp), CVD info Card
- **Parameters:** `r, g, b, colorName, isSaved, onSave, onRemove, onBack`
- **Navigation out:** ← back (to ScanColor or ColorLibrary depending on entry point)

---

## 5. Data Layer Reference

### UserRepository (`data/UserRepository.kt`)

**Storage:** `DataStore<Preferences>` named `user_prefs`. Singleton via `Context.userDataStore`.

**Keys:**
| Key | Type | Value |
|-----|------|-------|
| `user_id` | String | Google account email (unique ID) |
| `display_name` | String | Full name from Google |
| `email` | String | Google email |
| `photo_url` | String? | Profile picture URL |
| `theme_mode` | String? | `"LIGHT"` / `"DARK"` / absent (= SYSTEM) |
| `saved_colors` | String? | Pipe+semicolon delimited: `"name\|r\|g\|b;name\|r\|g\|b"` |

**Flows exposed:**
- `userFlow: Flow<UserData?>` — emits `null` when no session
- `themeFlow: Flow<ThemeMode>` — emits `SYSTEM` by default
- `savedColorsFlow: Flow<List<SavedColor>>` — parses saved_colors string; emits empty list by default

**Methods:** `saveUser(userData)`, `clearUser()` (preserves theme + saved colors), `cycleTheme(current)`, `saveColor(SavedColor)`, `removeColor(hex)`

### ColorNameDatabase (`data/ColorNameDatabase.kt`)
- ~150 colors (CSS + X11 palette)
- `nearest(r, g, b): ColorMatch` — converts input RGB to CIE Lab, returns the named color with the smallest Lab-space Euclidean distance
- Used only by `ColorAnalyzer` / camera scanner

### ColorLibraryData (`data/ColorLibraryData.kt`)
- `all: List<ColorEntry>` — 300+ entries with name, RGB, hex, category
- `simulate(r, g, b, cvdType): Triple<Int,Int,Int>` — applies CVD transformation matrix
- `ColorEntry(name, r, g, b, hex, category)`
- `ColorCategory` enum: ALL, MY_COLORS, WHITES, GRAYS, REDS, PINKS, ORANGES, YELLOWS, GREENS, TEALS, BLUES, PURPLES, BROWNS
- `CvdType` enum: DEUTERANOPIA ("D"), PROTANOPIA ("P"), TRITANOPIA ("T")

### Data Models

```kotlin
data class UserData(
    val userId: String,        // Google email — used as unique ID
    val displayName: String,
    val email: String,
    val photoUrl: String? = null
)

data class SavedColor(         // user-saved color from camera scanner
    val name: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val hex: String            // "#RRGGBB" format
)

data class DetectedColor(      // from ScanColorViewModel
    val r: Int, val g: Int, val b: Int,
    val hex: String,
    val name: String           // from ColorNameDatabase
)

sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    data class Success(val displayName: String, val email: String, val photoUrl: String?)
    data class Error(val message: String)
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AuthMode { LOGIN, SIGNUP }
```

---

## 6. Color System Reference

**All colors defined in:** `ui/theme/AppColorConfig.kt`
**No other file may contain raw hex values.**

### Palette (raw named hex values)

| Name | Hex | Usage |
|------|-----|-------|
| `IbmBlue` | `#0072B2` | Primary (light mode), button backgrounds |
| `SkyBlue` | `#56B4E9` | Primary (dark mode), tertiary (light) |
| `LightSkyBlue` | `#88CCEE` | Tertiary (dark mode) |
| `Amber` | `#E69F00` | Secondary (light mode) |
| `BrightAmber` | `#FFB700` | Secondary (dark mode) |
| `Vermillion` | `#D55E00` | Error (light mode) |
| `LightVermillion` | `#FF8C42` | Error (dark mode) |
| `DarkNavy` | `#0A1628` | Dark background |
| `NearWhite` | `#FAFAFA` | Light background |

### Light Mode Semantic Roles

| Token | Value |
|-------|-------|
| primary | IbmBlue `#0072B2` |
| secondary | Amber `#E69F00` |
| tertiary | SkyBlue `#56B4E9` |
| error | Vermillion `#D55E00` |
| background | NearWhite `#FAFAFA` |
| cardScanColor | `#CCE5F5` |
| cardColorLibrary | `#FFF0C2` |
| cardVisionModes | `#D6EEF8` |

### Dark Mode Semantic Roles

| Token | Value |
|-------|-------|
| primary | SkyBlue `#56B4E9` |
| secondary | BrightAmber `#FFB700` |
| tertiary | LightSkyBlue `#88CCEE` |
| error | LightVermillion `#FF8C42` |
| background | DarkNavy `#0A1628` |
| surface | `#162236` |

### CTA Card ("Color Vision Test" hero card)

| Mode | Background | Text | Button |
|------|-----------|------|--------|
| Light | `#FFF0C2` (light amber) | `#4A3600` (dark amber) | IbmBlue bg, white text |
| Dark | `MaterialTheme.colorScheme.surfaceVariant` | `onSurface` | IbmBlue bg, white text |

### Spectrum (logo iris only — not for UI)
Red, Orange, Yellow, Green, Blue, Violet — decorative, used in `ic_launcher_foreground.xml` and `HueIQLogo`.

---

## 7. Pending Work (TODOs)

| Priority | Area | Task |
|----------|------|------|
| High | **Ishihara Test** | Implement plate generation + scoring logic. Full spec in `walkthrough.md` §Ishihara Plate Generation. Use LCH color space + Poisson disk sampling. |
| Medium | **Profile photo** | Load `UserData.photoUrl` with Coil in HomeScreen avatar. Currently shows initials gradient. |
| Medium | **Email/password auth** | Wire the existing email+password fields in `SignInScreen` to a real auth provider (Firebase Auth or similar). |
| Low | **Vision Modes screen** | Implement the "Vision Modes" card destination — show the user how their surroundings would look with each CVD type. CVD simulation matrices already exist in `ColorLibraryData.kt`. |
| Low | **Real Ishihara scoring** | After plate generation: track user answers, determine CVD type + severity, show results. |

---

## 8. Build & Run

```powershell
# Required: point to Android Studio's bundled JDK
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Build debug APK
.\gradlew.bat assembleDebug

# Install to connected device/emulator
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Launch
adb shell am start -n "com.hueiq.app/.MainActivity"
```

**For a release build:** generate a release SHA-1 with `keytool`, add a second Android Client ID in Google Cloud Console, and sign the APK with a release keystore.

---

## 9. Rules for Future Sessions

These are non-negotiable constraints. Do not work around them.

0. **After every task: update both `CLAUDE.md` and `walkthrough.md` before reporting done.**
   - Update whichever section(s) describe what changed (screen, data layer, color system, TODOs, etc.)
   - Mark completed TODO items and add any newly discovered work
   - Add to the Development Journey section if a new architectural decision was made
   - This applies to every implementation task — features, bug fixes, refactors, anything

1. **Never put raw hex values in screen files.** All colors go in `AppColorConfig.kt` first. Screens reference only `MaterialTheme.colorScheme.*` tokens or named values re-exported from `AppColorConfig`.

2. **Dynamic color must stay disabled.** `dynamicColor = false` in `HueIQTheme`. The Ishihara test requires exact, predictable colors.

3. **DataStore is the only persistence layer.** No Room database, no SharedPreferences, no backend calls. `UserRepository` is the single gateway to DataStore.

4. **All new screens must use `MaterialTheme.colorScheme.*` tokens.** Check `AppColorConfig.kt` for semantic role names, not the raw palette values.

5. **The `AuthViewModel` is constructed once in `MainActivity`.** Do not construct it inside `NavGraph` or any screen — that creates a separate instance with its own DataStore read.

6. **When implementing Ishihara plates, use LCH color space.** RGB, HSV, and HSL will break the test. See `walkthrough.md` §Ishihara Plate Generation for the full Kotlin implementation.

7. **Do not add any new auth UI without wiring it.** The existing email/password fields are intentionally unwired. Adding more dead UI adds confusion.

8. **Google Sign-In credentials are already configured.** The Web Client ID and OAuth setup in `AuthViewModel.kt` are production values — do not change them for debug builds.

---

## 10. Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Compose BOM | 2026.02.01 | UI toolkit (Material3, Animation, Foundation) |
| Navigation Compose | 2.9.8 | Screen routing |
| Credentials | 1.6.0 | Google Credential Manager (OAuth sign-in) |
| Google Identity (GoogleID) | 1.2.0 | `GetGoogleIdOption`, ID token parsing |
| DataStore Preferences | 1.1.1 | Local key-value persistence |
| CameraX | 1.4.1 | Camera access (core, camera2, lifecycle, view) |
| Activity Compose | 1.8.0 | Compose ↔ Activity integration |
| Lifecycle ViewModel | 2.6.1 | `AndroidViewModel`, `viewModelScope` |

Managed via `gradle/libs.versions.toml` (version catalog). Do not add duplicate version declarations.

---

## 11. Manifest & Permissions

**File:** `app/src/main/AndroidManifest.xml`

**Permissions declared:**
- `android.permission.CAMERA` — requested at runtime in `ScanColorScreen`
- `android.hardware.camera` — declared as `required="false"` (app works without camera)

**Only one Activity:** `MainActivity` (exported, launcher).

No internet permission. No location. No storage. The app is intentionally minimal in permissions.
