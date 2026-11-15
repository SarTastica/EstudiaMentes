package com.example.estudiamentes.ui.screens

import androidx.compose.ui.graphics.Color

data class SalaLocalInfo(
    val nombre: String,
    val esPrivada: Boolean,
    val capacidadMaxima: Int,
    val participantes: List<EstudianteEstudio>
)

object SalasEstadoLocal {
    private val salas = mutableMapOf<String, SalaLocalInfo>()

    fun obtenerSala(nombreInicial: String): SalaLocalInfo {
        return salas.getOrPut(nombreInicial) {
            SalaLocalInfo(
                nombre = nombreInicial,
                esPrivada = false,
                capacidadMaxima = 8,
                participantes = listOf(
                    EstudianteEstudio("1", "Tú", 4.5f, Color(0xFF5A9BFF), isCurrentUser = true),
                    EstudianteEstudio("2", "Ana", 3.5f, Color(0xFF83B96D)),
                    EstudianteEstudio("3", "Carlos", 2.0f, Color(0xFFE99A9A)),
                    EstudianteEstudio("4", "Elena", 1.5f, Color(0xFFA99ACD)),
                    EstudianteEstudio("5", "Luis", 1.0f, Color(0xFFE8BD62))
                )
            )
        }
    }
}