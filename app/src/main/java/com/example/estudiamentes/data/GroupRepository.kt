
package com.example.estudiamentes.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

data class GrupoCreado(
    val groupId: String,
    val codigo: String
)

fun crearGrupo(
    nombre: String,
    privado: Boolean,
    adminId: String,
    onSuccess: (GrupoCreado) -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // Documento del grupo con ID automático
    val grupoRef = db.collection("groups").document()

    // Documento del creador dentro de members
    val miembroRef = grupoRef
        .collection("members")
        .document(adminId)

    fun intentarCrear(intentosRestantes: Int) {
        val codigo = Random.nextInt(100000, 1000000).toString()
        val codigoRef = db.collection("groupCodes").document(codigo)

        db.runTransaction { transaction ->
            val codigoExistente = transaction.get(codigoRef)

            if (codigoExistente.exists()) {
                throw IllegalStateException("CODIGO_DUPLICADO")
            }

            // Reservar código
            transaction.set(
                codigoRef,
                mapOf("groupId" to grupoRef.id)
            )

            // Guardar grupo
            transaction.set(
                grupoRef,
                mapOf(
                    "adminId" to adminId,
                    "codigo" to codigo,
                    "fechaCreacion" to FieldValue.serverTimestamp(),
                    "name" to nombre,
                    "privado" to privado
                )
            )

            // Registrar creador como administrador
            transaction.set(
                miembroRef,
                mapOf(
                    "uid" to adminId,
                    "role" to "admin",
                    "joinedAt" to FieldValue.serverTimestamp()
                )
            )

            codigo
        }.addOnSuccessListener { codigoCreado ->
            onSuccess(
                GrupoCreado(
                    groupId = grupoRef.id,
                    codigo = codigoCreado
                )
            )
        }.addOnFailureListener { error ->
            if (
                error.message?.contains("CODIGO_DUPLICADO") == true &&
                intentosRestantes > 1
            ) {
                intentarCrear(intentosRestantes - 1)
            } else {
                onError(error)
            }
        }
    }

    // Máximo de 5 intentos
    intentarCrear(5)
}
