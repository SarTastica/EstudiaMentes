package com.example.estudiamentes.model

import com.google.firebase.firestore.Exclude

data class SalaModel(
    val id: String = "",
    val name: String = "",
    val privado: Boolean = false,
    val codigo: String = "",
    val maxMembers: Int = 8,
    val adminId: String = "",
    val memberIds: List<String> = emptyList(),
    val etiquetas: List<String> = emptyList()
) {
    @get:Exclude
    val isPrivate: Boolean
        get() = privado
}