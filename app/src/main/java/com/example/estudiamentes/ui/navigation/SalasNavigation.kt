
package com.example.estudiamentes.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.estudiamentes.ui.screens.SalasScreen
import com.example.estudiamentes.ui.screens.CreateSalaScreen
import com.example.estudiamentes.ui.screens.SalaEstandarScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // PANTALLA PRINCIPAL DE SALAS
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

        // BUSCAR UNA SALA
        composable(SalasRoutes.BUSCAR) {
            SearchSalaScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // CREAR UNA SALA
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

        // SALA ESTÁNDAR
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

            // CONSULTAR LOS DATOS DE LA SALA EN FIRESTORE
            LaunchedEffect(groupId) {

                if (groupId.isNotBlank()) {

                    FirebaseFirestore.getInstance()
                        .collection("groups")
                        .document(groupId)
                        .get()
                        .addOnSuccessListener { document ->

                            if (document.exists()) {

                                // Obtener nombre de la sala
                                nombreSala = document.getString("name")
                                    ?: "Mi sala"

                                // Obtener administrador de Firestore
                                val adminId = document.getString("adminId")

                                // Obtener usuario conectado
                                val usuarioActual =
                                    FirebaseAuth.getInstance().currentUser?.uid

                                // Comprobar si es administrador
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
                    // Aquí conectaremos las opciones de administrador.
                }
            )
        }
    }
}
