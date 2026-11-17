package com.example.estudiamentes.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun saveProfile(
        name: String,
        username: String,
        studyLevel: String,
        center: String,
        branch: String,
        selectedDays: List<String>,
        studyHoursPerDay: Float,
        studyTime: String,
        mainGoal: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onResult(false, "No hay ningún usuario conectado")
            return
        }

        val profileData = hashMapOf(
            "nombre" to name,
            "username" to username,
            "nivelEstudios" to studyLevel,
            "centro" to center,
            "rama" to branch,
            "diasEstudio" to selectedDays,
            "horasPorDia" to studyHoursPerDay,
            "horaEstudio" to studyTime,
            "objetivo" to mainGoal,
            "perfilCompleto" to true
        )

        firestore.collection("users")
            .document(uid)
            .update(profileData as Map<String, Any>)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { exception ->
                onResult(
                    false,
                    "Error al guardar el perfil: ${exception.message}"
                )
            }
    }
}
