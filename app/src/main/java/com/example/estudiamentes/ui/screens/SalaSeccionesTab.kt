package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SalaApuntesTab(
    modifier: Modifier = Modifier
) {
    SeccionBaseSala(
        titulo = "Apuntes compartidos",
        subtitulo = "Sube y descarga documentos de estudio del grupo.",
        icono = "📚",
        modifier = modifier
    )
}

@Composable
fun SalaOpcionesTab(
    esAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    SeccionBaseSala(
        titulo = "Opciones de la sala",
        subtitulo = if (esAdmin) {
            "Gestiona los participantes y ajustes de la sala."
        } else {
            "Consulta la información y miembros de la sala."
        },
        icono = "⚙️",
        modifier = modifier
    )
}

@Composable
private fun SeccionBaseSala(
    titulo: String,
    subtitulo: String,
    icono: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EstudioColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icono, fontSize = 42.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EstudioColors.navy
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitulo,
                fontSize = 13.sp,
                color = EstudioColors.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}