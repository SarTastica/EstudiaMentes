package com.example.estudiamentes.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Obtenemos la ruta actual para saber qué botón del footer pintar de azul
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: BottomNavItem.Salas.route

    // El footer global se pinta una sola vez aquí
    MainScaffold(
        currentRoute = currentRoute,
        onNavigate = { route ->
            navController.navigate(route) {
                // Configuración estándar para no acumular pantallas al navegar por el footer
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    ) { paddingValues ->
        // Este NavHost es el "hueco" donde se cargan las páginas
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Salas.route
        ) {
            composable(BottomNavItem.Inicio.route) {
                PlaceholderScreen("Inicio", modifier = Modifier.padding(paddingValues))
            }

            composable(BottomNavItem.Salas.route) {
                // Aquí inyectamos tu pantalla independiente de Salas
                SalasNavigation(paddingValues = paddingValues)
            }

            composable(BottomNavItem.Mensajes.route) {
                PlaceholderScreen("Mensajes", modifier = Modifier.padding(paddingValues))
            }

            composable(BottomNavItem.Archivos.route) {
                PlaceholderScreen("Archivos", modifier = Modifier.padding(paddingValues))
            }

            composable(BottomNavItem.Perfil.route) {
                PlaceholderScreen("Perfil", modifier = Modifier.padding(paddingValues))
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