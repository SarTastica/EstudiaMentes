package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun SearchSalaScreen(
    onBack: () -> Unit,
    onSalaUnida: (String) -> Unit
) {
    var codigoSala by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }
    var isBuscando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Unirse a una Sala", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = codigoSala,
            onValueChange = { codigoSala = it },
            label = { Text("Código de la sala") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (mensajeError.isNotEmpty()) {
            Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Cancelar")
            }

            Button(
                onClick = {
                    if (codigoSala.isNotBlank()) {
                        isBuscando = true
                        mensajeError = ""

                        FirebaseFirestore.getInstance()
                            .collection("groups")
                            .document(codigoSala.trim())
                            .get()
                            .addOnSuccessListener { document ->
                                isBuscando = false
                                if (document.exists()) {
                                    onSalaUnida(document.id)
                                } else {
                                    mensajeError = "No se ha encontrado ninguna sala con ese código."
                                }
                            }
                            .addOnFailureListener {
                                isBuscando = false
                                mensajeError = "Error de conexión al buscar la sala."
                            }
                    }
                },
                enabled = !isBuscando && codigoSala.isNotBlank()
            ) {
                Text(if (isBuscando) "Buscando..." else "Unirse")
            }
        }
    }
}