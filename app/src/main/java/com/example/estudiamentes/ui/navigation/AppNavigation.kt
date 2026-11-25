package com.example.estudiamentes.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.estudiamentes.ui.screens.EditProfileScreen
import com.example.estudiamentes.ui.screens.ProfileScreen

@Composable
fun AppNavigation(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Salas.route

    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Salas.route
        ) {
            composable(BottomNavItem.Inicio.route) {
                PlaceholderScreen("Inicio", modifier = Modifier.padding(paddingValues))
            }

            composable(BottomNavItem.Salas.route) {
                SalasNavigation(paddingValues = paddingValues)
            }

            composable(BottomNavItem.Mensajes.route) {
                PlaceholderScreen("Mensajes", modifier = Modifier.padding(paddingValues))
            }

            composable(BottomNavItem.Archivos.route) {
                PlaceholderScreen("Archivos", modifier = Modifier.padding(paddingValues))
            }

            composable(BottomNavItem.Perfil.route) {
                ProfileScreen(
                    paddingValues = paddingValues,
                    onEditProfile = {
                        navController.navigate("edit_profile")
                    },
                    onLogout = onLogout
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    onCancel = {
                        navController.popBackStack()
                    },
                    onSave = { username, photoUri, dailyGoal ->
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

// Pantalla temporal para las pestañas que aún no hemos creado
@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla de $title en construcción")
    }
}