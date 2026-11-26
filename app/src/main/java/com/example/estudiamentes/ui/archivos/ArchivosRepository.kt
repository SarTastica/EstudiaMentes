package com.example.estudiamentes.ui.archivos

import android.content.Context
import android.content.Intent
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.io.File

class ArchivosRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observarUsuarioYFavoritos(
        uid: String,
        fallbackNombre: String,
        onUpdate: (nombre: String, favoritos: Set<String>, ocultos: Set<String>) -> Unit
    ): ListenerRegistration {
        return db.collection("users").document(uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("name")
                        ?: doc.getString("username")
                        ?: fallbackNombre

                    @Suppress("UNCHECKED_CAST")
                    val favoritos = doc.get("favoritosApuntes") as? List<String> ?: emptyList()

                    @Suppress("UNCHECKED_CAST")
                    val ocultos = doc.get("ocultosApuntes") as? List<String> ?: emptyList()

                    onUpdate(nombre, favoritos.toSet(), ocultos.toSet())
                }
            }
    }

    fun observarApuntes(
        onUpdate: (List<ApunteGeneral>) -> Unit
    ): ListenerRegistration {
        return db.collection("apuntes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ApunteGeneral::class.java)?.copy(id = doc.id)
                }
                onUpdate(lista)
            }
    }

    fun observarEtiquetas(
        onUpdate: (List<String>) -> Unit
    ): ListenerRegistration {
        val base = listOf("Matemáticas", "Fundamentos de los Computadores", "40983", "Programación", "Física")
        return db.collection("etiquetas_apuntes")
            .addSnapshotListener { snapshot, _ ->
                val personalizadas = snapshot?.documents?.mapNotNull { it.getString("nombre") } ?: emptyList()
                val combinadas = (base + personalizadas)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                onUpdate(combinadas)
            }
    }

    fun guardarEtiquetaPersonalizada(nombre: String) {
        val limpia = nombre.trim()
        if (limpia.isBlank()) return
        val docId = limpia.lowercase().replace(Regex("[^a-z0-9]"), "_")
        db.collection("etiquetas_apuntes").document(docId)
            .set(
                mapOf(
                    "nombre" to limpia,
                    "usos" to FieldValue.increment(1)
                ),
                SetOptions.merge()
            )
    }

    fun toggleFavorito(uid: String, apunteId: String, yaEsFavorito: Boolean) {
        val userRef = db.collection("users").document(uid)
        if (yaEsFavorito) {
            userRef.update("favoritosApuntes", FieldValue.arrayRemove(apunteId))
        } else {
            userRef.set(
                mapOf("favoritosApuntes" to FieldValue.arrayUnion(apunteId)),
                SetOptions.merge()
            )
        }
    }

    fun ocultarApunte(uid: String, apunteId: String, onSuccess: () -> Unit) {
        val userRef = db.collection("users").document(uid)
        userRef.set(
            mapOf(
                "ocultosApuntes" to FieldValue.arrayUnion(apunteId),
                "favoritosApuntes" to FieldValue.arrayRemove(apunteId)
            ),
            SetOptions.merge()
        ).addOnSuccessListener { onSuccess() }
    }

    fun restaurarOcultos(uid: String, onSuccess: () -> Unit) {
        db.collection("users").document(uid)
            .update("ocultosApuntes", emptyList<String>())
            .addOnSuccessListener { onSuccess() }
    }

    fun borrarApunte(
        uid: String,
        apunteId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        db.collection("apuntes").document(apunteId)
            .delete()
            .addOnSuccessListener {
                db.collection("users").document(uid)
                    .update("favoritosApuntes", FieldValue.arrayRemove(apunteId))
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "No se pudo borrar el archivo")
            }
    }

    fun subirApunte(
        uid: String,
        autorNombre: String,
        titulo: String,
        descripcion: String,
        categoria: String,
        etiqueta: String,
        tipo: String,
        esPublico: Boolean,
        url: String = "",
        base64Data: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (etiqueta.isNotBlank()) {
            guardarEtiquetaPersonalizada(etiqueta)
        }
        val nuevoApunte = hashMapOf(
            "titulo" to titulo,
            "descripcion" to descripcion,
            "categoria" to categoria,
            "etiqueta" to etiqueta,
            "tipo" to tipo,
            "url" to url,
            "base64Data" to base64Data,
            "autorNombre" to autorNombre,
            "autorId" to uid,
            "esPublico" to esPublico,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("apuntes")
            .add(nuevoApunte)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Error al guardar") }
    }

    fun abrirApunteConSistema(
        context: Context,
        apunte: ApunteGeneral,
        onFallbackFoto: (ApunteGeneral) -> Unit = {},
        onFallbackPdf: (ApunteGeneral) -> Unit = {}
    ) {
        when {
            apunte.tipo == "FOTO" && apunte.base64Data.isNotBlank() -> {
                try {
                    val cleanBase64 = apunte.base64Data.replace("\\s".toRegex(), "")
                    val imgBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val safeName = apunte.titulo.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                        .let { if (it.endsWith(".jpg", true) || it.endsWith(".png", true)) it else "$it.jpg" }
                    val tempFile = File(context.cacheDir, safeName)
                    tempFile.writeBytes(imgBytes)

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        tempFile
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "image/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(Intent.createChooser(intent, "Abrir imagen con"))
                    } else {
                        onFallbackFoto(apunte)
                    }
                } catch (_: Exception) {
                    onFallbackFoto(apunte)
                }
            }
            apunte.tipo == "PDF" && apunte.base64Data.isNotBlank() -> {
                try {
                    val cleanBase64 = apunte.base64Data.replace("\\s".toRegex(), "")
                    val pdfBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val safeName = apunte.titulo.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                        .let { if (it.endsWith(".pdf", true)) it else "$it.pdf" }
                    val tempFile = File(context.cacheDir, safeName)
                    tempFile.writeBytes(pdfBytes)

                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        tempFile
                    )
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(Intent.createChooser(intent, "Abrir PDF con"))
                    } else {
                        onFallbackPdf(apunte)
                    }
                } catch (_: Exception) {
                    onFallbackPdf(apunte)
                }
            }
            apunte.url.isNotBlank() -> {
                try {
                    val urlFinal = if (
                        apunte.url.startsWith("http://") || apunte.url.startsWith("https://")
                    ) {
                        apunte.url
                    } else {
                        "https://${apunte.url}"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, urlFinal.toUri()).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "No se pudo abrir el enlace",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {
                Toast.makeText(context, "Archivo sin contenido", Toast.LENGTH_SHORT).show()
            }
        }
    }
}