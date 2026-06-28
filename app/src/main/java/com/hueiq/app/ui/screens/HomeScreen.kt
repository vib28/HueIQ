package com.hueiq.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueiq.app.ui.theme.AppColorConfig
import com.hueiq.app.ui.theme.AppDarkBackground
import com.hueiq.app.ui.theme.AppPrimary
import com.hueiq.app.ui.theme.AppSecondary
import com.hueiq.app.ui.theme.AppTertiary
import com.hueiq.app.ui.theme.LocalDarkTheme
import com.hueiq.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    displayName: String,
    email: String = "",
    photoUrl: String? = null,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onToggleTheme: () -> Unit = {},
    onSignOut: () -> Unit,
    onStartIshiharaTest: () -> Unit = {},
    onScanColor: () -> Unit = {},
    onColorLibrary: () -> Unit = {}
) {
    val isDark = LocalDarkTheme.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "HueIQ",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Theme toggle: cycles SYSTEM → LIGHT → DARK → SYSTEM
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.LIGHT  -> Icons.Outlined.LightMode
                                ThemeMode.DARK   -> Icons.Outlined.DarkMode
                                ThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
                            },
                            contentDescription = "Toggle theme"
                        )
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Sign out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // User avatar with gradient
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AppPrimary, AppTertiary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Welcome back,",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = displayName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (email.isNotBlank()) {
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Ishihara Color Vision Test — primary CTA card ──
            // Light: amber card (matches Color Library tile)
            // Dark:  surfaceVariant (matches all other feature tiles)
            val ctaCardBg     = if (isDark) MaterialTheme.colorScheme.surfaceVariant else AppColorConfig.CtaCard.lightBackground
            val ctaText       = if (isDark) MaterialTheme.colorScheme.onSurface       else AppColorConfig.CtaCard.lightText
            val ctaSubtext    = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else AppColorConfig.CtaCard.lightSubtext
            val ctaIconBg     = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else AppColorConfig.CtaCard.lightIconBg
            val ctaIconTint   = if (isDark) MaterialTheme.colorScheme.tertiary         else AppColorConfig.CtaCard.lightIcon
            val ctaBtnBg      = AppColorConfig.CtaCard.lightButtonBg
            val ctaBtnText    = AppColorConfig.CtaCard.lightButtonText

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartIshiharaTest() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ctaCardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ctaIconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RemoveRedEye,
                            contentDescription = null,
                            tint = ctaIconTint,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Color Vision Test",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ctaText
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "Take the Ishihara test to detect colorblindness type",
                            fontSize = 13.sp,
                            color = ctaSubtext
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ctaBtnBg
                    ) {
                        Text(
                            text = "Start",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ctaBtnText
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "More Tools",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Feature cards
            FeatureCard(
                icon = Icons.Outlined.CameraAlt,
                title = "Scan Color",
                description = "Point your camera at any object to identify its colors",
                cardColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant
                            else AppColorConfig.Light.cardScanColor,
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = onScanColor
            )

            Spacer(Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Outlined.Palette,
                title = "Color Library",
                description = "Browse named colors with colorblind-friendly descriptions",
                cardColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant
                            else AppColorConfig.Light.cardColorLibrary,
                iconTint = MaterialTheme.colorScheme.secondary,
                onClick = onColorLibrary
            )

            Spacer(Modifier.height(12.dp))

            FeatureCard(
                icon = Icons.Outlined.Accessibility,
                title = "Vision Modes",
                description = "Simulate different types of color vision deficiency",
                cardColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant
                            else AppColorConfig.Light.cardVisionModes,
                iconTint = MaterialTheme.colorScheme.tertiary
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onScanColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorConfig.ButtonFilled.containerColor,
                    contentColor   = AppColorConfig.ButtonFilled.contentColor
                )
            ) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Start Scanning",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    cardColor: Color,
    iconTint: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(displayName = "Alex Johnson", onSignOut = {})
}
