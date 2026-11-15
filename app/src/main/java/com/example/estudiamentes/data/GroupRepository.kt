package com.example.estudiamentes.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

// Tiempo máximo de inactividad antes de eliminar una sala automáticamente (4 horas)
const val HORAS_INACTIVIDAD_MAX = 4L
private const val MS_INACTIVIDAD_MAX = HORAS_INACTIVIDAD_MAX * 60 * 60 * 1000L

data class GrupoCreado(
    val groupId: String,
    val codigo: String
)

private fun obtenerNombreRealUsuario(
    db: FirebaseFirestore,
    uid: String,
    onResult: (String) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
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

fun crearGrupo(
    nombre: String,
    privado: Boolean,
    adminId: String,
    etiquetas: List<String> = emptyList(),
    onSuccess: (GrupoCreado) -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val grupoRef = db.collection("groups").document()
    val miembroRef = grupoRef.collection("members").document(adminId)

    obtenerNombreRealUsuario(db, adminId) { nombreAdmin ->
        fun intentarCrear(intentosRestantes: Int) {
            val codigo = Random.nextInt(100000, 1000000).toString()
            val codigoRef = db.collection("groupCodes").document(codigo)
            val ahora = System.currentTimeMillis()

            db.runTransaction { transaction ->
                val codigoExistente = transaction.get(codigoRef)

                if (codigoExistente.exists()) {
                    throw IllegalStateException("CODIGO_DUPLICADO")
                }

                transaction.set(
                    codigoRef,
                    mapOf("groupId" to grupoRef.id)
                )

                transaction.set(
                    grupoRef,
                    mapOf(
                        "adminId" to adminId,
                        "codigo" to codigo,
                        "fechaCreacion" to FieldValue.serverTimestamp(),
                        "lastActivity" to ahora,
                        "name" to nombre,
                        "privado" to privado,
                        "maxMembers" to 8,
                        "memberIds" to listOf(adminId),
                        "etiquetas" to etiquetas
                    )
                )

                transaction.set(
                    miembroRef,
                    mapOf(
                        "uid" to adminId,
                        "name" to nombreAdmin,
                        "role" to "admin",
                        "studyHours" to 0.0,
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

        intentarCrear(5)
    }
}

data class GrupoEncontrado(
    val groupId: String,
    val nombre: String,
    val privado: Boolean
)

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
                        db.collection("groupCodes").document(codigoLimpio).delete()
                        onError(Exception("La sala ya no existe."))
                        return@addOnSuccessListener
                    }

                    val nombre = grupoDocument.getString("name") ?: "Sala sin nombre"
                    val privado = grupoDocument.getBoolean("privado")
                        ?: grupoDocument.getBoolean("isPrivate")
                        ?: false

                    onSuccess(
                        GrupoEncontrado(
                            groupId = groupId,
                            nombre = nombre,
                            privado = privado
                        )
                    )
                }
                .addOnFailureListener { error -> onError(error) }
        }
        .addOnFailureListener { error -> onError(error) }
}

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
    val miembroRef = grupoRef.collection("members").document(usuario.uid)

    obtenerNombreRealUsuario(db, usuario.uid) { nombreUsuario ->
        db.runTransaction { transaction ->
            val grupo = transaction.get(grupoRef)
            if (!grupo.exists()) {
                throw IllegalStateException("La sala ya no existe.")
            }

            val ahora = System.currentTimeMillis()
            val miembroExistente = transaction.get(miembroRef)

            if (miembroExistente.exists()) {
                transaction.update(grupoRef, "lastActivity", ahora)
                false
            } else {
                val maxMembers = (grupo.getLong("maxMembers") ?: 8L).toInt()
                @Suppress("UNCHECKED_CAST")
                val memberIds = grupo.get("memberIds") as? List<String> ?: emptyList()

                if (memberIds.size >= maxMembers) {
                    throw IllegalStateException("La sala está llena ($maxMembers/$maxMembers usuarios).")
                }

                transaction.update(
                    grupoRef,
                    mapOf(
                        "memberIds" to FieldValue.arrayUnion(usuario.uid),
                        "lastActivity" to ahora
                    )
                )
                transaction.set(
                    miembroRef,
                    mapOf(
                        "uid" to usuario.uid,
                        "name" to nombreUsuario,
                        "role" to "member",
                        "studyHours" to 0.0,
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
}

fun registrarActividadGrupo(groupId: String) {
    if (groupId.isBlank()) return
    FirebaseFirestore.getInstance()
        .collection("groups")
        .document(groupId)
        .update("lastActivity", System.currentTimeMillis())
}

fun eliminarSalaCompleta(
    groupId: String,
    codigo: String = "",
    onComplete: () -> Unit = {}
) {
    if (groupId.isBlank()) {
        onComplete()
        return
    }
    val db = FirebaseFirestore.getInstance()
    val groupRef = db.collection("groups").document(groupId)

    groupRef.get().addOnSuccessListener { doc ->
        val codigoReal = codigo.ifBlank { doc.getString("codigo") ?: "" }
        if (codigoReal.isNotBlank()) {
            db.collection("groupCodes").document(codigoReal).delete()
        }

        listOf("members", "messages", "notes").forEach { sub ->
            groupRef.collection(sub).get().addOnSuccessListener { snap ->
                snap.documents.forEach { it.reference.delete() }
            }
        }

        groupRef.delete().addOnCompleteListener { onComplete() }
    }.addOnFailureListener {
        onComplete()
    }
}

fun salirDeGrupo(
    groupId: String,
    uid: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit = {}
) {
    if (groupId.isBlank() || uid.isBlank()) {
        onSuccess()
        return
    }

    val db = FirebaseFirestore.getInstance()
    val groupRef = db.collection("groups").document(groupId)
    val memberRef = groupRef.collection("members").document(uid)

    db.runTransaction { transaction ->
        val grupo = transaction.get(groupRef)
        if (!grupo.exists()) return@runTransaction "YA_NO_EXISTE"

        @Suppress("UNCHECKED_CAST")
        val memberIds = (grupo.get("memberIds") as? List<String> ?: emptyList()).toMutableList()
        memberIds.remove(uid)

        val adminId = grupo.getString("adminId") ?: ""
        val codigo = grupo.getString("codigo") ?: ""

        transaction.delete(memberRef)

        if (memberIds.isEmpty()) {
            if (codigo.isNotBlank()) {
                transaction.delete(db.collection("groupCodes").document(codigo))
            }
            transaction.delete(groupRef)
            "SALA_ELIMINADA"
        } else {
            val updates = mutableMapOf<String, Any>(
                "memberIds" to memberIds,
                "lastActivity" to System.currentTimeMillis()
            )
            if (adminId == uid) {
                val nuevoAdmin = memberIds.first()
                updates["adminId"] = nuevoAdmin
                transaction.update(groupRef.collection("members").document(nuevoAdmin), "role", "admin")
            }
            transaction.update(groupRef, updates)
            "MIEMBRO_SALIO"
        }
    }.addOnSuccessListener {
        onSuccess()
    }.addOnFailureListener { error ->
        onError(error)
    }
}

fun limpiarSalasInactivas() {
    val db = FirebaseFirestore.getInstance()
    val ahora = System.currentTimeMillis()

    db.collection("groups").get().addOnSuccessListener { snapshot ->
        for (doc in snapshot.documents) {
            @Suppress("UNCHECKED_CAST")
            val memberIds = doc.get("memberIds") as? List<String>

            if (memberIds != null && memberIds.isEmpty()) {
                eliminarSalaCompleta(doc.id, doc.getString("codigo") ?: "")
                continue
            }

            val lastActivity = doc.getLong("lastActivity")
                ?: doc.getTimestamp("fechaCreacion")?.toDate()?.time
                ?: continue

            if (ahora - lastActivity > MS_INACTIVIDAD_MAX) {
                eliminarSalaCompleta(doc.id, doc.getString("codigo") ?: "")
            }
        }
    }
}