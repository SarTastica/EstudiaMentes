package com.example.estudiamentes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estudiamentes.ui.HomeScreen
import com.example.estudiamentes.ui.setup.ProfileScreen
import com.example.estudiamentes.ui.setup.ProfileSetupScreen

sealed class MainScreen(val route: String) {
    object Home : MainScreen("home")
    object Profile : MainScreen("profile")
}

@Composable
fun MainNavGraph(welcomeMessage: String? = null, onLogout: () -> Unit) {

    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = MainScreen.Home.route
    ) {
        composable(MainScreen.Home.route) {
            HomeScreen(welcomeMessage = welcomeMessage, onLogout = onLogout, onProfileClick = {
                navController.navigate(MainScreen.Profile.route)
            })
        }

        composable(MainScreen.Profile.route) {
            ProfileScreen(
                onEditProfile = {
                    navController.navigate(MainScreen.Profile.route)
                }
            )
        }
    }
}
