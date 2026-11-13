package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(24.dp)
    ) {
        Text(
            text = "Unirse a una Sala",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF64B5F6)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = codigoSala,
            onValueChange = {
                codigoSala = it
                mensajeError = ""
            },
            label = { Text("Código de la sala") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF64B5F6),
                cursorColor = Color(0xFF64B5F6)
            )
        )

        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensajeError, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Cancelar", color = Color.Gray)
            }

            Button(
                onClick = {
                    if (codigoSala.isNotBlank()) {
                        isBuscando = true
                        mensajeError = ""

                        FirebaseFirestore.getInstance()
                            .collection("groups")
                            .whereEqualTo("codigo", codigoSala.trim())
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                isBuscando = false
                                if (!querySnapshot.isEmpty) {
                                    val document = querySnapshot.documents[0]
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
                enabled = !isBuscando && codigoSala.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isBuscando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Unirse", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}