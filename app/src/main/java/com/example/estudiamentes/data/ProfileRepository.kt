package com.example.estudiamentes.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
            "perfilCompleto" to true,
            "manzanas" to 15,
            "rachaDias" to 0,
            "ultimaFechaEstudio" to "",
            "itemsDesbloqueados" to emptyList<String>(),
            "itemsEquipados" to emptyList<String>()
        )

        firestore.collection("users")
            .document(uid)
            .set(profileData, SetOptions.merge())
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

    fun cargarEstadoHuerto(
        context: Context,
        onLoaded: (manzanas: Int, racha: Int, desbloqueados: List<String>, equipados: List<String>) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: "guest"
        val prefs = context.getSharedPreferences("estudiamentes_huerto_$uid", Context.MODE_PRIVATE)

        val localManzanas = prefs.getInt("manzanas", 15)
        val localRacha = prefs.getInt("rachaDias", 0)
        val localDesbloqueados = prefs.getStringSet("desbloqueados", emptySet())?.toList() ?: emptyList()
        val localEquipados = prefs.getStringSet("equipados", emptySet())?.toList() ?: emptyList()

        if (uid == "guest") {
            onLoaded(localManzanas, localRacha, localDesbloqueados, localEquipados)
            return
        }

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val manzanas = doc.getLong("manzanas")?.toInt() ?: localManzanas
                val racha = doc.getLong("rachaDias")?.toInt() ?: localRacha
                val desbloqueados = (doc.get("itemsDesbloqueados") as? List<*>)
                    ?.filterIsInstance<String>() ?: localDesbloqueados
                val equipados = (doc.get("itemsEquipados") as? List<*>)
                    ?.filterIsInstance<String>() ?: localEquipados

                prefs.edit()
                    .putInt("manzanas", manzanas)
                    .putInt("rachaDias", racha)
                    .putStringSet("desbloqueados", desbloqueados.toSet())
                    .putStringSet("equipados", equipados.toSet())
                    .apply()

                onLoaded(manzanas, racha, desbloqueados, equipados)
            }
            .addOnFailureListener {
                onLoaded(localManzanas, localRacha, localDesbloqueados, localEquipados)
            }
    }

    fun registrarCosechaEstudio(
        context: Context,
        minutosEstudiados: Int,
        onUpdated: (nuevasManzanas: Int, nuevaRacha: Int, ganadas: Int) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: "guest"
        val prefs = context.getSharedPreferences("estudiamentes_huerto_$uid", Context.MODE_PRIVATE)

        val hoy = LocalDate.now().toString()
        val ultimaFecha = prefs.getString("ultimaFechaEstudio", "") ?: ""
        var rachaActual = prefs.getInt("rachaDias", 0)

        rachaActual = when {
            ultimaFecha == hoy -> if (rachaActual == 0) 1 else rachaActual
            ultimaFecha.isNotEmpty() -> {
                val diasDiferencia = runCatching {
                    ChronoUnit.DAYS.between(LocalDate.parse(ultimaFecha), LocalDate.now())
                }.getOrDefault(2L)
                if (diasDiferencia == 1L) rachaActual + 1 else 1
            }
            else -> 1
        }

        val baseManzanas = (minutosEstudiados / 2).coerceAtLeast(5)
        val multiplicador = if (rachaActual >= 3) 1.5 else 1.0
        val ganadas = (baseManzanas * multiplicador).toInt()
        val totalManzanas = prefs.getInt("manzanas", 15) + ganadas

        prefs.edit()
            .putInt("manzanas", totalManzanas)
            .putInt("rachaDias", rachaActual)
            .putString("ultimaFechaEstudio", hoy)
            .apply()

        if (uid != "guest") {
            val data = hashMapOf(
                "manzanas" to totalManzanas,
                "rachaDias" to rachaActual,
                "ultimaFechaEstudio" to hoy
            )
            firestore.collection("users").document(uid).set(data, SetOptions.merge())
        }

        onUpdated(totalManzanas, rachaActual, ganadas)
    }

    fun guardarPersonalizacionHuerto(
        context: Context,
        manzanas: Int,
        desbloqueados: List<String>,
        equipados: List<String>
    ) {
        val uid = auth.currentUser?.uid ?: "guest"
        val prefs = context.getSharedPreferences("estudiamentes_huerto_$uid", Context.MODE_PRIVATE)

        prefs.edit()
            .putInt("manzanas", manzanas)
            .putStringSet("desbloqueados", desbloqueados.toSet())
            .putStringSet("equipados", equipados.toSet())
            .apply()

        if (uid != "guest") {
            val data = hashMapOf(
                "manzanas" to manzanas,
                "itemsDesbloqueados" to desbloqueados,
                "itemsEquipados" to equipados
            )
            firestore.collection("users").document(uid).set(data, SetOptions.merge())
        }
    }
}