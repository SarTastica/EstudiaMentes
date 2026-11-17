package com.example.estudiamentes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estudiamentes.ui.screens.EditProfileScreen
import com.example.estudiamentes.ui.screens.ProfileScreen

sealed class MainScreen(val route: String) {
    object Home : MainScreen("home")
    object Profile : MainScreen("profile")
    object EditProfile : MainScreen("edit_profile")
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

        // Pantalla de perfil
        composable(MainScreen.Profile.route) {
            ProfileScreen(
                onEditProfile = {
                    navController.navigate(MainScreen.EditProfile.route)
                },
                onLogout = onLogout
            )
        }
        composable(MainScreen.EditProfile.route) {
            EditProfileScreen(
                onCancel = {
                    navController.popBackStack()
                },
                onSave = { username, photoUri, dailyGoal ->
                    // Más adelante conectaremos aquí Firebase.
                    navController.popBackStack()
                }
            )
        }
    }
}
