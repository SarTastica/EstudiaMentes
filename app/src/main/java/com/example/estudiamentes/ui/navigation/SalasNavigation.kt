package com.example.estudiamentes.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.estudiamentes.ui.screens.CreateSalaScreen
import com.example.estudiamentes.ui.screens.SalaEstandarScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.estudiamentes.ui.screens.SalasScreen
import com.example.estudiamentes.ui.screens.SearchSalaScreen

private object SalasRoutes {
    const val INICIO = "salas_inicio"
    const val BUSCAR = "buscar_sala"
    const val CREAR = "crear_sala"
    const val SALA_ESTANDAR = "sala_estandar"
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
                paddingValues = paddingValues,
                onBuscarSala = { navController.navigate(SalasRoutes.BUSCAR) },
                onCrearSala = { navController.navigate(SalasRoutes.CREAR) },
                onEntrarSala = { groupId -> navController.navigate("${SalasRoutes.SALA_ESTANDAR}/$groupId") }
            )
        }

        composable(SalasRoutes.BUSCAR) {
            SearchSalaScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSalaUnida = { groupId ->
                    navController.navigate(
                        "${SalasRoutes.SALA_ESTANDAR}/${Uri.encode(groupId)}"
                    ) {
                        popUpTo(SalasRoutes.INICIO)
                    }
                }
            )
        }

        composable(SalasRoutes.CREAR) {
            CreateSalaScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSalaCreada = { groupId ->
                    navController.navigate(
                        "${SalasRoutes.SALA_ESTANDAR}/${Uri.encode(groupId)}"
                    ) {
                        popUpTo(SalasRoutes.INICIO)
                    }
                }
            )
        }

        composable(
            route = "${SalasRoutes.SALA_ESTANDAR}/{groupId}",
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val groupId = backStackEntry.arguments
                ?.getString("groupId")
                ?: ""

            var nombreSala by remember(groupId) {
                mutableStateOf("Cargando sala...")
            }

            var esAdmin by remember(groupId) {
                mutableStateOf(false)
            }

            LaunchedEffect(groupId) {
                if (groupId.isNotBlank()) {
                    FirebaseFirestore.getInstance()
                        .collection("groups")
                        .document(groupId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                nombreSala = document.getString("name")
                                    ?: "Mi sala"

                                val adminId = document.getString("adminId")
                                val usuarioActual =
                                    FirebaseAuth.getInstance().currentUser?.uid

                                esAdmin = adminId != null &&
                                        adminId == usuarioActual
                            } else {
                                nombreSala = "Sala no encontrada"
                                esAdmin = false
                            }
                        }
                        .addOnFailureListener {
                            nombreSala = "Error al cargar la sala"
                            esAdmin = false
                        }
                }
            }

            SalaEstandarScreen(
                nombreSala = nombreSala,
                esAdmin = esAdmin,
                onBack = {
                    navController.popBackStack()
                },
                onOpcionesClick = {
                }
            )
        }
    }
}