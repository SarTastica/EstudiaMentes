
package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun CreateSalaScreen(
    onBack: () -> Unit = {}
) {
    var nombreSala by remember { mutableStateOf("") }
    var codigoGenerado by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF64B5F6)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Crear sala",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF64B5F6)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Crea un espacio para estudiar y compartir.",
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Nombre de la sala",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = nombreSala,
            onValueChange = {
                nombreSala = it
                codigoGenerado = null
            },
            label = { Text("Nombre de la sala") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                cursorColor = Color(0xFF64B5F6)
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Foto de la sala (opcional)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Imagen provisional de sala",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Añade una imagen para tu sala",
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Medium
                        //TODO hacer un boton de añadir imagen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                codigoGenerado = Random.nextInt(100000, 1000000).toString()
            },
            enabled = nombreSala.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64B5F6),
                disabledContainerColor = Color(0xFFBBDEFB)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Crear sala",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

    }
}
