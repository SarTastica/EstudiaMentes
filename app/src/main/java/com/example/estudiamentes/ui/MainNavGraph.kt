package com.example.estudiamentes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estudiamentes.ui.HomeScreen

sealed class MainScreen(val route: String) {
    object Home : MainScreen("home")
}

@Composable
fun MainNavGraph(welcomeMessage: String? = null, onLogout: () -> Unit) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainScreen.Home.route
    ) {
        composable(MainScreen.Home.route) {
            HomeScreen(welcomeMessage = welcomeMessage, onLogout = onLogout)
        }
    }
}
