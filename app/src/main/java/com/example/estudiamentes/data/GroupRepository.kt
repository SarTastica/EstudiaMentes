
package com.example.estudiamentes.data

import com.google.firebase.auth.FirebaseAuth
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
    etiquetas: List<String> = emptyList(),
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
                    "privado" to privado,
                    "etiquetas" to etiquetas
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

data class GrupoEncontrado(
    val groupId: String,
    val nombre: String,
    val privado: Boolean
)

// BUSCAR UNA SALA MEDIANTE SU CÓDIGO
fun buscarGrupoPorCodigo(
    codigo: String,
    onSuccess: (GrupoEncontrado) -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val codigoLimpio = codigo.trim()

    if (codigoLimpio.length != 6 || !codigoLimpio.all { it.isDigit() }) {
        onError(Exception("Introduce un código válido de 6 dígitos."))
        return
    }

    db.collection("groupCodes")
        .document(codigoLimpio)
        .get()
        .addOnSuccessListener { codigoDocument ->

            if (!codigoDocument.exists()) {
                onError(Exception("No existe ninguna sala con ese código."))
                return@addOnSuccessListener
            }

            val groupId = codigoDocument.getString("groupId")

            if (groupId.isNullOrBlank()) {
                onError(Exception("No se ha podido identificar la sala."))
                return@addOnSuccessListener
            }

            db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener { grupoDocument ->

                    if (!grupoDocument.exists()) {
                        onError(Exception("La sala ya no existe."))
                        return@addOnSuccessListener
                    }

                    val nombre = grupoDocument.getString("name")
                        ?: "Sala sin nombre"

                    val privado = grupoDocument.getBoolean("privado")
                        ?: false

                    onSuccess(
                        GrupoEncontrado(
                            groupId = groupId,
                            nombre = nombre,
                            privado = privado
                        )
                    )
                }
                .addOnFailureListener { error ->
                    onError(error)
                }
        }
        .addOnFailureListener { error ->
            onError(error)
        }
}

// UNIRSE A UNA SALA
fun unirseAGrupo(
    groupId: String,
    onSuccess: (Boolean) -> Unit,
    onError: (Exception) -> Unit
) {
    val usuario = FirebaseAuth.getInstance().currentUser

    if (usuario == null) {
        onError(Exception("Debes iniciar sesión para unirte a una sala."))
        return
    }

    val db = FirebaseFirestore.getInstance()
    val grupoRef = db.collection("groups").document(groupId)

    val miembroRef = grupoRef
        .collection("members")
        .document(usuario.uid)

    db.runTransaction { transaction ->

        val grupo = transaction.get(grupoRef)

        if (!grupo.exists()) {
            throw IllegalStateException("La sala ya no existe.")
        }

        val miembroExistente = transaction.get(miembroRef)

        if (miembroExistente.exists()) {
            // Ya pertenece a la sala
            false
        } else {
            transaction.set(
                miembroRef,
                mapOf(
                    "uid" to usuario.uid,
                    "role" to "member",
                    "joinedAt" to FieldValue.serverTimestamp()
                )
            )

            true
        }
    }.addOnSuccessListener { seHaUnido ->
        onSuccess(seHaUnido)
    }.addOnFailureListener { error ->
        onError(error)
    }
}
