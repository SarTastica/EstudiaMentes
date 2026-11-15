package com.example.estudiamentes.ui.screens

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

data class SalaFirestore(
    val id: String = "",
    val name: String = "",
    val subject: String = "General",
    val isPrivate: Boolean = false,
    val inviteCode: String = "",
    val maxMembers: Int = 8,
    val adminId: String = "",
    val memberIds: List<String> = emptyList(),
    val createdAt: Long = 0L
)

object SalasRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun generarCodigoSala(): String {
        val numero = Random.nextInt(1000, 9999)
        return "EST-$numero"
    }

    private fun obtenerNombreUsuario(uid: String, onResult: (String) -> Unit) {
        val currentUser = auth.currentUser
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("name")
                    ?: doc.getString("username")
                    ?: currentUser?.displayName
                    ?: currentUser?.email?.substringBefore("@")
                    ?: "Estudiante"
                onResult(nombre)
            }
            .addOnFailureListener {
                val fallback = currentUser?.displayName
                    ?: currentUser?.email?.substringBefore("@")
                    ?: "Estudiante"
                onResult(fallback)
            }
    }

    fun crearSala(
        nombre: String,
        asignatura: String = "Asignatura general",
        esPrivada: Boolean,
        capacidadMaxima: Int = 8,
        onSuccess: (groupId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Debes iniciar sesión para crear una sala")
            return
        }

        val uid = currentUser.uid
        val capacidadValida = capacidadMaxima.coerceIn(2, 8)
        val codigoInvitacion = generarCodigoSala()

        obtenerNombreUsuario(uid) { nombreUsuario ->
            val nuevaSalaRef = db.collection("groups").document()
            val groupId = nuevaSalaRef.id

            val datosSala = hashMapOf(
                "id" to groupId,
                "name" to nombre.trim(),
                "subject" to asignatura.trim().ifEmpty { "Asignatura general" },
                "isPrivate" to esPrivada,
                "inviteCode" to codigoInvitacion,
                "maxMembers" to capacidadValida,
                "adminId" to uid,
                "memberIds" to listOf(uid),
                "createdAt" to System.currentTimeMillis()
            )

            val datosMiembroAdmin = hashMapOf(
                "uid" to uid,
                "name" to nombreUsuario,
                "studyHours" to 0.0,
                "joinedAt" to System.currentTimeMillis()
            )

            val batch = db.batch()
            batch.set(nuevaSalaRef, datosSala)
            batch.set(nuevaSalaRef.collection("members").document(uid), datosMiembroAdmin)

            batch.commit()
                .addOnSuccessListener {
                    onSuccess(groupId)
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Error al crear la sala")
                }
        }
    }

    fun unirsePorCodigo(
        codigoIngresado: String,
        onSuccess: (groupId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("Debes iniciar sesión para unirte a una sala")
            return
        }

        val uid = currentUser.uid
        val codigoLimpio = codigoIngresado.trim().uppercase()

        if (codigoLimpio.isEmpty()) {
            onError("Introduce un código válido")
            return
        }

        db.collection("groups")
            .whereEqualTo("inviteCode", codigoLimpio)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onError("No existe ninguna sala con ese código")
                    return@addOnSuccessListener
                }

                val docSala = querySnapshot.documents.first()
                val sala = docSala.toObject(SalaFirestore::class.java)?.copy(id = docSala.id)
                if (sala == null) {
                    onError("Error al leer los datos de la sala")
                    return@addOnSuccessListener
                }

                unirseASalaExistente(
                    sala = sala,
                    onSuccess = { onSuccess(sala.id) },
                    onError = onError
                )
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Error al buscar la sala")
            }
    }

    fun unirseASalaExistente(
        sala: SalaFirestore,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onError("Usuario no autenticado")
        val uid = currentUser.uid

        if (sala.memberIds.contains(uid)) {
            onSuccess()
            return
        }

        if (sala.memberIds.size >= sala.maxMembers) {
            onError("La sala está llena (${sala.maxMembers}/${sala.maxMembers})")
            return
        }

        obtenerNombreUsuario(uid) { nombreUsuario ->
            val groupRef = db.collection("groups").document(sala.id)
            val datosMiembro = hashMapOf(
                "uid" to uid,
                "name" to nombreUsuario,
                "studyHours" to 0.0,
                "joinedAt" to System.currentTimeMillis()
            )

            val batch = db.batch()
            batch.update(groupRef, "memberIds", FieldValue.arrayUnion(uid))
            batch.set(groupRef.collection("members").document(uid), datosMiembro)

            batch.commit()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "No se pudo unir a la sala")
                }
        }
    }
}