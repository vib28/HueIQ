package com.hueiq.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hueiq.app.data.SavedColor
import com.hueiq.app.ui.auth.AuthViewModel
import com.hueiq.app.ui.auth.SignInState
import com.hueiq.app.ui.screens.ColorDetailScreen
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
    object ColorDetail   : Screen("color_detail/{r}/{g}/{b}/{name}") {
        fun createRoute(r: Int, g: Int, b: Int, name: String) =
            "color_detail/$r/$g/$b/${Uri.encode(name)}"
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val themeMode by authViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val savedColors by authViewModel.savedColors.collectAsState()

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
                onBack = { navController.popBackStack() },
                savedColors = savedColors,
                onSaveColor = { r, g, b, name ->
                    authViewModel.saveColor(SavedColor(name, r, g, b, "#%02X%02X%02X".format(r, g, b)))
                },
                onViewDetails = { r, g, b, name ->
                    navController.navigate(Screen.ColorDetail.createRoute(r, g, b, name))
                }
            )
        }

        composable(Screen.ColorLibrary.route) {
            ColorLibraryScreen(
                onBack = { navController.popBackStack() },
                savedColors = savedColors,
                onColorClick = { r, g, b, name ->
                    navController.navigate(Screen.ColorDetail.createRoute(r, g, b, name))
                }
            )
        }

        composable(
            route = Screen.ColorDetail.route,
            arguments = listOf(
                navArgument("r") { type = NavType.IntType },
                navArgument("g") { type = NavType.IntType },
                navArgument("b") { type = NavType.IntType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val r = backStackEntry.arguments?.getInt("r") ?: 0
            val g = backStackEntry.arguments?.getInt("g") ?: 0
            val b = backStackEntry.arguments?.getInt("b") ?: 0
            val name = backStackEntry.arguments?.getString("name") ?: ""
            val hex = "#%02X%02X%02X".format(r, g, b)
            val isSaved = savedColors.any { it.hex == hex }

            ColorDetailScreen(
                r = r,
                g = g,
                b = b,
                colorName = name,
                isSaved = isSaved,
                onSave = {
                    authViewModel.saveColor(SavedColor(name, r, g, b, hex))
                },
                onRemove = {
                    authViewModel.removeColor(hex)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
