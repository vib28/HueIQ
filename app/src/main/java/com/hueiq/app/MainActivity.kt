package com.hueiq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.hueiq.app.navigation.AppNavigation
import com.hueiq.app.ui.auth.AuthViewModel
import com.hueiq.app.ui.theme.HueIQTheme
import com.hueiq.app.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by authViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            HueIQTheme(themeMode = themeMode) {
                AppNavigation(authViewModel = authViewModel)
            }
        }
    }
}
