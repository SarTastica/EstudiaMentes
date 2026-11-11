package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SalaEstandarScreen(
    nombreSala: String,
    esAdmin: Boolean = false,
    onBack: () -> Unit = {},
    onClasificacionClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onArchivosClick: () -> Unit = {},
    onOpcionesClick: () -> Unit = {}
) {
    val fondo = Color(0xFFF5F5F5)

    Scaffold(
        containerColor = fondo
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(paddingValues)
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {

            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color(0xFF344054)
                    )
                }

                Text(
                    text = nombreSala,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263449)
                )

                if (esAdmin) {
                    IconButton(onClick = onOpcionesClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Opciones de administrador",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tu espacio de estudio",
                fontSize = 15.sp,
                color = Color(0xFF7A8492),
                modifier = Modifier.padding(start = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Clasificación
            ApartadoSala(
                titulo = "Clasificación",
                descripcion = "Consulta los puntos y las posiciones de los miembros.",
                icono = Icons.Default.Leaderboard,
                colorFondo = Color(0xFFDCEEFF),
                colorIcono = Color(0xFF3478B9),
                onClick = onClasificacionClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Chat
            ApartadoSala(
                titulo = "Chat",
                descripcion = "Habla con los demás integrantes de la sala.",
                icono = Icons.Default.Message,
                colorFondo = Color(0xFFFFF1C9),
                colorIcono = Color(0xFFB28A22),
                onClick = onChatClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Archivos
            ApartadoSala(
                titulo = "Archivos",
                descripcion = "Comparte y consulta documentos de estudio.",
                icono = Icons.Default.Folder,
                colorFondo = Color(0xFFFBE0E0),
                colorIcono = Color(0xFFB65353),
                onClick = onArchivosClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Opciones
            ApartadoSala(
                titulo = "Opciones",
                descripcion = "Gestiona la sala y sus participantes.",
                icono = Icons.Default.Settings,
                colorFondo = Color(0xFFE5E7EB),
                colorIcono = Color(0xFF596579),
                onClick = onOpcionesClick
            )
        }
    }
}

@Composable
private fun ApartadoSala(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    colorFondo: Color,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = colorIcono,
                modifier = Modifier.size(42.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp)
            ) {
                Text(
                    text = titulo,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263449)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descripcion,
                    fontSize = 13.sp,
                    color = Color(0xFF596579),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
