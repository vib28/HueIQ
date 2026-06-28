package com.hueiq.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hueiq.app.ui.auth.AuthViewModel
import com.hueiq.app.ui.auth.SignInState
import com.hueiq.app.ui.screens.ColorLibraryScreen
import com.hueiq.app.ui.screens.HomeScreen
import com.hueiq.app.ui.screens.IshiharaTestScreen
import com.hueiq.app.ui.screens.LoadingScreen
import com.hueiq.app.ui.screens.ScanColorScreen
import com.hueiq.app.ui.screens.SignInScreen
import com.hueiq.app.ui.theme.ThemeMode

sealed class Screen(val route: String) {
    object Loading      : Screen("loading")
    object SignIn       : Screen("signin")
    object Home         : Screen("home")
    object IshiharaTest  : Screen("ishihara_test")
    object ScanColor     : Screen("scan_color")
    object ColorLibrary  : Screen("color_library")
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val themeMode by authViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    NavHost(navController = navController, startDestination = Screen.Loading.route) {

        composable(Screen.Loading.route) {
            LoadingScreen(
                viewModel = authViewModel,
                onResult = { isLoggedIn ->
                    val destination = if (isLoggedIn) Screen.Home.route else Screen.SignIn.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignIn.route) {
            SignInScreen(
                viewModel = authViewModel,
                onSignInSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val signInState by authViewModel.signInState.collectAsState()
            val success = signInState as? SignInState.Success
            HomeScreen(
                displayName     = success?.displayName ?: "User",
                email           = success?.email ?: "",
                photoUrl        = success?.photoUrl,
                themeMode       = themeMode,
                onToggleTheme   = { authViewModel.cycleTheme(themeMode) },
                onSignOut       = {
                    authViewModel.signOut()
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onStartIshiharaTest = {
                    navController.navigate(Screen.IshiharaTest.route)
                },
                onScanColor = {
                    navController.navigate(Screen.ScanColor.route)
                },
                onColorLibrary = {
                    navController.navigate(Screen.ColorLibrary.route)
                }
            )
        }

        composable(Screen.IshiharaTest.route) {
            IshiharaTestScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ScanColor.route) {
            ScanColorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ColorLibrary.route) {
            ColorLibraryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
