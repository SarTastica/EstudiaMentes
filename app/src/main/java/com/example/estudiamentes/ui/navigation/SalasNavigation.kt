
package com.example.estudiamentes.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.estudiamentes.ui.screens.SalasScreen
import com.example.estudiamentes.ui.screens.CreateSalaScreen

private object SalasRoutes {
    const val INICIO = "salas_inicio"
    const val BUSCAR = "buscar_sala"

    const val CREAR = "crear_sala"
}

@Composable
fun SalasNavigation(paddingValues: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SalasRoutes.INICIO,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(SalasRoutes.INICIO) {
            SalasScreen(
                paddingValues = PaddingValues(0.dp),
                onBuscarSala = {
                    navController.navigate(SalasRoutes.BUSCAR)
                },
                onCrearSala = {
                    navController.navigate(SalasRoutes.CREAR)
                }
            )
        }

        composable(SalasRoutes.BUSCAR) {
            SearchSalaScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(SalasRoutes.CREAR) {
            CreateSalaScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
