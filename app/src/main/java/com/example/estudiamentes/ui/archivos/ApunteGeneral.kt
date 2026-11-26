package com.example.estudiamentes.ui.archivos

data class ApunteGeneral(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Apuntes",
    val etiqueta: String = "",
    val tipo: String = "ENLACE",
    val url: String = "",
    val base64Data: String = "",
    val autorNombre: String = "",
    val autorId: String = "",
    val esPublico: Boolean = true,
    val timestamp: Long = 0L
)