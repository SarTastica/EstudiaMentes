package com.example.estudiamentes.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.estudiamentes.data.registrarActividadGrupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ApunteSala(
    val id: String = "",
    val titulo: String = "",
    val tipo: String = "ENLACE",
    val url: String = "",
    val base64Data: String = "",
    val autorNombre: String = "",
    val autorId: String = "",
    val timestamp: Long = 0L
)

@Composable
fun SalaApuntesTab(
    groupId: String,
    nombreSala: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    var apuntes by remember { mutableStateOf<List<ApunteSala>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var nombreUsuarioActual by remember { mutableStateOf("Estudiante") }

    var mostrarDialogoEnlace by remember { mutableStateOf(false) }
    var tituloEnlace by remember { mutableStateOf("") }
    var urlEnlace by remember { mutableStateOf("") }
    var fotoSeleccionada by remember { mutableStateOf<ApunteSala?>(null) }

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    nombreUsuarioActual = doc.getString("name")
                        ?: doc.getString("username")
                                ?: currentUser.displayName
                                ?: currentUser.email?.substringBefore("@")
                                ?: "Estudiante"
                }
        }
    }

    DisposableEffect(groupId) {
        if (groupId.isBlank()) return@DisposableEffect onDispose {}

        val listener = db.collection("groups")
            .document(groupId)
            .collection("notes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                apuntes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ApunteSala::class.java)?.copy(id = doc.id)
                }
            }

        onDispose { listener.remove() }
    }

    fun guardarApunteFirestore(
        titulo: String,
        tipo: String,
        url: String = "",
        base64Data: String = ""
    ) {
        if (groupId.isBlank()) return
        isUploading = true
        val nuevoApunte = hashMapOf(
            "titulo" to titulo,
            "tipo" to tipo,
            "url" to url,
            "base64Data" to base64Data,
            "autorNombre" to nombreUsuarioActual,
            "autorId" to (currentUser?.uid ?: "guest"),
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("groups")
            .document(groupId)
            .collection("notes")
            .add(nuevoApunte)
            .addOnSuccessListener {
                registrarActividadGrupo(groupId)
                isUploading = false
                Toast.makeText(context, "Apunte compartido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                isUploading = false
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null && groupId.isNotBlank()) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val base64Str = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

            val fechaStr = SimpleDateFormat("dd-MM_HHmm", Locale.getDefault()).format(Date())
            val fileName = "Foto_apunte_$fechaStr.jpg"
            guardarApunteFirestore(titulo = fileName, tipo = "FOTO", base64Data = base64Str)
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && groupId.isNotBlank()) {
            var fileName = "Documento.pdf"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex)
                }
            }

            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                if (bytes.size > 700_000) {
                    Toast.makeText(
                        context,
                        "El PDF supera 700 KB. Compártelo como enlace de Drive.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val base64Str = Base64.encodeToString(bytes, Base64.DEFAULT)
                    guardarApunteFirestore(titulo = fileName, tipo = "PDF", base64Data = base64Str)
                }
            }
        }
    }

    if (fotoSeleccionada != null) {
        val apunteFoto = fotoSeleccionada!!
        val bitmap = remember(apunteFoto.base64Data) {
            try {
                val decoded = Base64.decode(apunteFoto.base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
            } catch (_: Exception) {
                null
            }
        }

        AlertDialog(
            onDismissRequest = { fotoSeleccionada = null },
            containerColor = Color.White,
            title = {
                Text(
                    text = apunteFoto.titulo,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy
                )
            },
            text = {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = apunteFoto.titulo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text("No se pudo cargar la imagen.")
                }
            },
            confirmButton = {
                TextButton(onClick = { fotoSeleccionada = null }) {
                    Text("Cerrar", color = EstudioColors.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (mostrarDialogoEnlace) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEnlace = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Compartir enlace",
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tituloEnlace,
                        onValueChange = { tituloEnlace = it },
                        label = { Text("Título (ej. Resumen Tema 2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = urlEnlace,
                        onValueChange = { urlEnlace = it },
                        label = { Text("URL (https://...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tituloLimpio = tituloEnlace.trim().ifEmpty { "Enlace compartido" }
                        var urlLimpia = urlEnlace.trim()
                        if (urlLimpia.isNotEmpty()) {
                            if (!urlLimpia.startsWith("http://") && !urlLimpia.startsWith("https://")) {
                                urlLimpia = "https://$urlLimpia"
                            }
                            guardarApunteFirestore(
                                titulo = tituloLimpio,
                                tipo = "ENLACE",
                                url = urlLimpia
                            )
                            tituloEnlace = ""
                            urlEnlace = ""
                            mostrarDialogoEnlace = false
                        }
                    },
                    enabled = urlEnlace.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = EstudioColors.primary)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEnlace = false }) {
                    Text("Cancelar", color = EstudioColors.secondaryText)
                }
            }
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = EstudioColors.white),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Apuntes de $nombreSala",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EstudioColors.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = "Sube fotos de la pizarra, PDFs ligeros o enlaces útiles",
                fontSize = 12.sp,
                color = EstudioColors.secondaryText,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonSubidaApunte(
                    icono = Icons.Default.CameraAlt,
                    texto = "Foto",
                    colorFondo = Color(0xFFE7F1FF),
                    colorContenido = EstudioColors.primary,
                    enabled = !isUploading,
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.weight(1f)
                )
                BotonSubidaApunte(
                    icono = Icons.Default.PictureAsPdf,
                    texto = "PDF",
                    colorFondo = Color(0xFFFDECEC),
                    colorContenido = Color(0xFFE05252),
                    enabled = !isUploading,
                    onClick = { pdfLauncher.launch("application/pdf") },
                    modifier = Modifier.weight(1f)
                )
                BotonSubidaApunte(
                    icono = Icons.Default.Link,
                    texto = "Enlace",
                    colorFondo = EstudioColors.meadow,
                    colorContenido = Color(0xFF2E7D32),
                    enabled = !isUploading,
                    onClick = { mostrarDialogoEnlace = true },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isUploading) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = EstudioColors.primary,
                    trackColor = EstudioColors.primarySoft
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = EstudioColors.sky,
                thickness = 1.dp
            )

            if (apuntes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📂", fontSize = 38.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay apuntes todavía",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )
                        Text(
                            text = "Usa los botones superiores para compartir material.",
                            fontSize = 12.sp,
                            color = EstudioColors.secondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(apuntes, key = { it.id }) { apunte ->
                        ApunteItemCard(
                            apunte = apunte,
                            onClick = {
                                when {
                                    apunte.tipo == "FOTO" && apunte.base64Data.isNotBlank() -> {
                                        fotoSeleccionada = apunte
                                    }
                                    apunte.tipo == "PDF" && apunte.base64Data.isNotBlank() -> {
                                        try {
                                            val pdfBytes = Base64.decode(apunte.base64Data, Base64.DEFAULT)
                                            val tempFile = File(context.cacheDir, apunte.titulo)
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
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(
                                                context,
                                                "No se pudo abrir el PDF en este dispositivo",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    apunte.url.isNotBlank() -> {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, apunte.url.toUri())
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(
                                                context,
                                                "No se pudo abrir el enlace",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonSubidaApunte(
    icono: ImageVector,
    texto: String,
    colorFondo: Color,
    colorContenido: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = colorFondo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                tint = colorContenido,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorContenido
            )
        }
    }
}

@Composable
private fun ApunteItemCard(
    apunte: ApunteSala,
    onClick: () -> Unit
) {
    val (icono, colorIcono, fondoIcono) = when (apunte.tipo) {
        "FOTO" -> Triple(Icons.Default.Image, EstudioColors.primary, EstudioColors.primarySoft)
        "PDF" -> Triple(Icons.Default.PictureAsPdf, Color(0xFFE05252), Color(0xFFFDECEC))
        else -> Triple(Icons.Default.Link, Color(0xFF2E7D32), EstudioColors.meadow)
    }

    val fechaTexto = remember(apunte.timestamp) {
        if (apunte.timestamp > 0L) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(apunte.timestamp))
        } else {
            ""
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = EstudioColors.sky,
        border = BorderStroke(1.dp, EstudioColors.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = fondoIcono
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = apunte.tipo,
                        tint = colorIcono,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apunte.titulo,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Subido por ${apunte.autorNombre} • $fechaTexto",
                    fontSize = 11.sp,
                    color = EstudioColors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Abrir",
                tint = EstudioColors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}