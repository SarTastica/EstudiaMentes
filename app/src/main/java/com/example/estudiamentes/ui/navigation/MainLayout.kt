package com.example.estudiamentes.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : BottomNavItem("inicio", "Inicio", Icons.Default.Home)
    object Salas : BottomNavItem("salas", "Salas", Icons.Default.Groups)
    object Estudio : BottomNavItem("estudio", "Estudio", Icons.Default.HourglassEmpty)
    object Archivos : BottomNavItem("archivos", "Archivos", Icons.Default.InsertDriveFile)
    object Perfil : BottomNavItem("perfil", "Perfil", Icons.Default.Person)
}

@Composable
fun EstudiamentesBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Salas,
        BottomNavItem.Estudio,
        BottomNavItem.Archivos,
        BottomNavItem.Perfil
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1E88E5),
                    unselectedIconColor = Color(0xFF90CAF9),
                    selectedTextColor = Color(0xFF1E88E5),
                    unselectedTextColor = Color(0xFF90CAF9),
                    indicatorColor = Color(0xFFE3F2FD)
                )
            )
        }
    }
}

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
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        content(paddingValues)
    }
}