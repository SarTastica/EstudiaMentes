package com.example.estudiamentes.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// 1. Definimos las rutas y datos de los 5 botones del diseño
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : BottomNavItem("inicio", "Inicio", Icons.Default.Home)
    object Salas : BottomNavItem("salas", "Salas", Icons.Default.Groups)
    object Mensajes : BottomNavItem("mensajes", "Mensajes", Icons.Default.Chat)
    object Archivos : BottomNavItem("archivos", "Archivos", Icons.Default.InsertDriveFile)
    object Perfil : BottomNavItem("perfil", "Perfil", Icons.Default.Person)
}

// 2. Componente visual del Footer (NavigationBar)
@Composable
fun EstudiamentesBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Salas,
        BottomNavItem.Mensajes,
        BottomNavItem.Archivos,
        BottomNavItem.Perfil
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp // Da la pequeña sombra superior que se ve en el diseño
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1E88E5), // Azul intenso activo
                    unselectedIconColor = Color(0xFF90CAF9), // Azul claro inactivo
                    selectedTextColor = Color(0xFF1E88E5),
                    unselectedTextColor = Color(0xFF90CAF9),
                    indicatorColor = Color(0xFFE3F2FD) // Fondo sutil al seleccionar
                )
            )
        }
    }
}

// 3. El contenedor "Molde" que usarás para todas las pantallas principales
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            EstudiamentesBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigate
            )
        },
        containerColor = Color(0xFFF5F5F5) // Fondo grisáceo claro general de la app
    ) { paddingValues ->
        // El contenido de la pantalla (ej. SalasScreen) se inyecta aquí
        content(paddingValues)
    }
}