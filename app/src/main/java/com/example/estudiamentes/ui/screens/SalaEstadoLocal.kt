package com.example.estudiamentes.ui.screens

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color

data class EstadoSalaLocal(
    val nombre: String,
    val esPrivada: Boolean = false,
    val capacidadMaxima: Int = 8,
    val estudiantes: List<EstudianteEstudio> = listOf(
        EstudianteEstudio("Tú", 4.5f, Color(0xFF5A9BFF)),
        EstudianteEstudio("Alex", 3.0f, Color(0xFF83B96D)),
        EstudianteEstudio("Lucía", 2.0f, Color(0xFFE99A9A)),
        EstudianteEstudio("Carlos", 1.5f, Color(0xFFA99ACD)),
        EstudianteEstudio("Sofía", 1.0f, Color(0xFFE8BD62))
    ).sortedByDescending { it.horas }
)

object SalasEstadoLocal {
    private val estadosPorSala = mutableStateMapOf<String, EstadoSalaLocal>()

    fun obtenerEstado(groupId: String, nombreInicial: String): EstadoSalaLocal {
        val clave = groupId.ifBlank { nombreInicial }
        val existente = estadosPorSala[clave]

        if (existente != null) {
            if ((existente.nombre.isBlank() || existente.nombre == "Cargando sala...") &&
                nombreInicial.isNotBlank() && nombreInicial != "Cargando sala..."
            ) {
                val actualizado = existente.copy(nombre = nombreInicial)
                estadosPorSala[clave] = actualizado
                return actualizado
            }
            return existente
        }

        val nuevo = EstadoSalaLocal(nombre = nombreInicial)
        if (nombreInicial.isNotBlank() && nombreInicial != "Cargando sala...") {
            estadosPorSala[clave] = nuevo
        }
        return nuevo
    }

    fun obtenerNombreActualizado(groupId: String, nombreOriginal: String): String {
        val clave = groupId.ifBlank { nombreOriginal }
        val guardado = estadosPorSala[clave]?.nombre
        return if (!guardado.isNullOrBlank() && guardado != "Cargando sala...") {
            guardado
        } else {
            nombreOriginal
        }
    }

    fun guardarCambios(
        groupId: String,
        nombreInicial: String,
        nuevoNombre: String,
        nuevaPrivacidad: Boolean,
        nuevaCapacidad: Int,
        nuevaLista: List<EstudianteEstudio>
    ) {
        val clave = groupId.ifBlank { nombreInicial }
        estadosPorSala[clave] = EstadoSalaLocal(
            nombre = nuevoNombre,
            esPrivada = nuevaPrivacidad,
            capacidadMaxima = nuevaCapacidad,
            estudiantes = nuevaLista.sortedByDescending { it.horas }
        )
    }
}