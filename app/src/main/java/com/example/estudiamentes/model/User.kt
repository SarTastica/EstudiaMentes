package com.example.estudiamentes.model

data class User(
    val uid: String = "",
    val email: String = "",
    val manzanas: Int = 0,
    val rachaDias: Int = 0,
    val ultimaFechaEstudio: String = "",
    val itemsDesbloqueados: List<String> = emptyList(),
    val itemsEquipados: List<String> = emptyList()
)