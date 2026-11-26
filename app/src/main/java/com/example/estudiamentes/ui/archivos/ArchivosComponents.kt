package com.example.estudiamentes.ui.archivos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.createBitmap
import com.example.estudiamentes.ui.screens.EstudioColors
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchivoItemCard(
    apunte: ApunteGeneral,
    esPropio: Boolean,
    esFavorito: Boolean,
    mostrarBadgePrivacidad: Boolean,
    onToggleFavorito: () -> Unit,
    onBorrarOOcultar: () -> Unit,
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
        color = Color.White,
        border = BorderStroke(1.dp, EstudioColors.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = apunte.titulo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EstudioColors.navy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (mostrarBadgePrivacidad) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (apunte.esPublico) Icons.Default.Public else Icons.Default.Lock,
                            contentDescription = if (apunte.esPublico) "Público" else "Privado",
                            tint = EstudioColors.secondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (apunte.categoria.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EstudioColors.primarySoft
                        ) {
                            Text(
                                text = apunte.categoria,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EstudioColors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    if (apunte.etiqueta.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = EstudioColors.meadow
                        ) {
                            Text(
                                text = apunte.etiqueta,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (apunte.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = apunte.descripcion,
                        fontSize = 12.sp,
                        color = EstudioColors.navy.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Por ${apunte.autorNombre} • $fechaTexto",
                    fontSize = 11.sp,
                    color = EstudioColors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorito) {
                Icon(
                    imageVector = if (esFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) Color(0xFFFFB300) else EstudioColors.secondaryText
                )
            }

            IconButton(onClick = onBorrarOOcultar) {
                Icon(
                    imageVector = if (esPropio) Icons.Default.DeleteOutline else Icons.Default.VisibilityOff,
                    contentDescription = if (esPropio) "Borrar archivo" else "Ocultar archivo",
                    tint = if (esPropio) Color(0xFFE05252) else EstudioColors.secondaryText
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogoSubirArchivo(
    etiquetasDisponibles: List<String>,
    etiquetasFrecuentes: List<String>,
    onDismiss: () -> Unit,
    onGuardar: (
        titulo: String,
        descripcion: String,
        categoria: String,
        etiqueta: String,
        tipo: String,
        esPublico: Boolean,
        url: String,
        base64Data: String
    ) -> Unit
) {
    val context = LocalContext.current
    val tipos = listOf("FOTO", "PDF", "ENLACE")
    val categorias = listOf("Apuntes", "Examen", "Ejercicios", "Resumen", "Esquema")

    var tipoSeleccionado by remember { mutableStateOf("FOTO") }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Apuntes") }
    var busquedaEtiqueta by remember { mutableStateOf("") }
    var etiquetaSeleccionada by remember {
        mutableStateOf(etiquetasFrecuentes.firstOrNull() ?: etiquetasDisponibles.firstOrNull() ?: "")
    }
    var esPublico by remember { mutableStateOf(true) }
    var urlEnlace by remember { mutableStateOf("") }
    var base64Adjunto by remember { mutableStateOf("") }
    var nombreAdjunto by remember { mutableStateOf("") }

    val etiquetasSugeridas = remember(busquedaEtiqueta, etiquetasDisponibles, etiquetasFrecuentes, etiquetaSeleccionada) {
        val query = busquedaEtiqueta.trim().lowercase()
        if (query.isEmpty()) {
            val frecuentes = etiquetasFrecuentes.ifEmpty { etiquetasDisponibles.take(5) }
            if (etiquetaSeleccionada.isNotBlank() && frecuentes.none { it.equals(etiquetaSeleccionada, true) }) {
                (listOf(etiquetaSeleccionada) + frecuentes).take(6)
            } else {
                frecuentes.take(6)
            }
        } else {
            etiquetasDisponibles.filter { it.lowercase().contains(query) }.take(8)
        }
    }

    val textoNuevoTag = busquedaEtiqueta.trim()
    val mostrarOpcionCrearTag = textoNuevoTag.isNotBlank() &&
            etiquetasDisponibles.none { it.equals(textoNuevoTag, ignoreCase = true) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)
            base64Adjunto = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val fechaStr = SimpleDateFormat("dd-MM_HHmm", Locale.getDefault()).format(Date())
            nombreAdjunto = "Foto_$fechaStr.jpg"
            if (titulo.isBlank()) {
                titulo = "Foto apuntes $fechaStr"
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
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
                        "El PDF supera 700 KB. Súbelo como Enlace de Drive.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    base64Adjunto = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    nombreAdjunto = fileName
                    if (titulo.isBlank()) {
                        titulo = fileName.removeSuffix(".pdf")
                    }
                }
            }
        }
    }

    val etiquetaFinal = etiquetaSeleccionada.ifBlank { textoNuevoTag }
    val formularioValido = titulo.isNotBlank() && etiquetaFinal.isNotBlank() && when (tipoSeleccionado) {
        "ENLACE" -> urlEnlace.isNotBlank()
        else -> base64Adjunto.isNotBlank()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Nuevo archivo o apunte",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EstudioColors.navy
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = EstudioColors.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            tipos.forEachIndexed { index, tipo ->
                                SegmentedButton(
                                    selected = tipoSeleccionado == tipo,
                                    onClick = {
                                        tipoSeleccionado = tipo
                                        base64Adjunto = ""
                                        nombreAdjunto = ""
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = tipos.size),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = EstudioColors.primary,
                                        activeContentColor = Color.White,
                                        inactiveContainerColor = Color.White,
                                        inactiveContentColor = EstudioColors.navy
                                    )
                                ) {
                                    Text(
                                        text = when (tipo) {
                                            "FOTO" -> "Foto"
                                            "PDF" -> "PDF"
                                            else -> "Enlace"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        when (tipoSeleccionado) {
                            "FOTO" -> {
                                OutlinedButton(
                                    onClick = { cameraLauncher.launch() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = if (base64Adjunto.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = if (base64Adjunto.isNotBlank()) Color(0xFF2E7D32) else EstudioColors.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (nombreAdjunto.isNotBlank()) nombreAdjunto else "Hacer foto con cámara",
                                        color = EstudioColors.navy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            "PDF" -> {
                                OutlinedButton(
                                    onClick = { pdfLauncher.launch("application/pdf") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(
                                        imageVector = if (base64Adjunto.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = if (base64Adjunto.isNotBlank()) Color(0xFF2E7D32) else Color(0xFFE05252)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (nombreAdjunto.isNotBlank()) nombreAdjunto else "Seleccionar PDF (máx. 700 KB)",
                                        color = EstudioColors.navy,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            else -> {
                                OutlinedTextField(
                                    value = urlEnlace,
                                    onValueChange = { urlEnlace = it },
                                    label = { Text("URL (https://...)") },
                                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEDF0F5))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Nombre del archivo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción breve (opcional)") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEDF0F5))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Categoría",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categorias.forEach { cat ->
                                FilterChip(
                                    selected = categoriaSeleccionada == cat,
                                    onClick = { categoriaSeleccionada = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EstudioColors.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFEDF0F5))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Etiqueta / Asignatura",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )

                        OutlinedTextField(
                            value = busquedaEtiqueta,
                            onValueChange = { busquedaEtiqueta = it },
                            placeholder = { Text("Buscar o crear asignatura / código...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (busquedaEtiqueta.isNotEmpty()) {
                                    IconButton(onClick = { busquedaEtiqueta = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Text(
                            text = if (busquedaEtiqueta.isBlank()) "Más frecuentes" else "Coincidencias",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EstudioColors.secondaryText
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            etiquetasSugeridas.forEach { tag ->
                                FilterChip(
                                    selected = etiquetaSeleccionada.equals(tag, ignoreCase = true),
                                    onClick = {
                                        etiquetaSeleccionada = tag
                                        busquedaEtiqueta = ""
                                    },
                                    label = { Text(tag, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E7D32),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            if (mostrarOpcionCrearTag) {
                                FilterChip(
                                    selected = etiquetaSeleccionada.equals(textoNuevoTag, ignoreCase = true),
                                    onClick = {
                                        etiquetaSeleccionada = textoNuevoTag
                                        busquedaEtiqueta = ""
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = { Text("Crear \"$textoNuevoTag\"", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2E7D32),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF6F8FB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (esPublico) Icons.Default.Public else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = EstudioColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (esPublico) "Archivo público" else "Archivo privado",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EstudioColors.navy
                                    )
                                    Text(
                                        text = if (esPublico) "Visible para todos los estudiantes" else "Solo visible para ti",
                                        fontSize = 11.sp,
                                        color = EstudioColors.secondaryText
                                    )
                                }
                            }
                            Switch(
                                checked = esPublico,
                                onCheckedChange = { esPublico = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = EstudioColors.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = EstudioColors.secondaryText, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            var urlLimpia = urlEnlace.trim()
                            if (tipoSeleccionado == "ENLACE" && urlLimpia.isNotEmpty() &&
                                !urlLimpia.startsWith("http://") && !urlLimpia.startsWith("https://")
                            ) {
                                urlLimpia = "https://$urlLimpia"
                            }
                            onGuardar(
                                titulo.trim(),
                                descripcion.trim(),
                                categoriaSeleccionada,
                                etiquetaFinal,
                                tipoSeleccionado,
                                esPublico,
                                if (tipoSeleccionado == "ENLACE") urlLimpia else "",
                                if (tipoSeleccionado != "ENLACE") base64Adjunto else ""
                            )
                        },
                        enabled = formularioValido,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EstudioColors.primary)
                    ) {
                        Text(
                            text = "Subir",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoPrevisualizarFoto(
    apunte: ApunteGeneral,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val bitmap = remember(apunte.base64Data) {
        try {
            val cleanBase64 = apunte.base64Data.replace("\\s".toRegex(), "")
            val decoded = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
        } catch (_: Exception) {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = apunte.titulo,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                offset = if (scale > 1f) offset + pan else Offset.Zero
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = apunte.titulo,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("No se pudo cargar la imagen.", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoVisorPdfPantallaCompleta(
    apunte: ApunteGeneral,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var paginaActual by remember { mutableIntStateOf(0) }
    var totalPaginas by remember { mutableIntStateOf(0) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val pdfFile = remember(apunte.id, apunte.base64Data) {
        try {
            val cleanBase64 = apunte.base64Data.replace("\\s".toRegex(), "")
            val pdfBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            val file = File(context.cacheDir, "visor_${apunte.id.ifEmpty { "temp" }}.pdf")
            file.writeBytes(pdfBytes)
            file
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(pdfFile) {
        onDispose { pdfFile?.delete() }
    }

    val pageBitmap = remember(pdfFile, paginaActual) {
        if (pdfFile == null || !pdfFile.exists()) return@remember null
        try {
            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    totalPaginas = renderer.pageCount
                    if (totalPaginas == 0) return@remember null
                    val index = paginaActual.coerceIn(0, totalPaginas - 1)
                    renderer.openPage(index).use { page ->
                        val width = (page.width * 2).coerceAtLeast(800)
                        val height = (page.height * 2).coerceAtLeast(1100)
                        val bmp = createBitmap(width, height)
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bmp
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1E2229)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF14171C))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = apunte.titulo,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (totalPaginas > 0) {
                            Text(
                                text = "Página ${paginaActual + 1} de $totalPaginas (pellizca para zoom)",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(paginaActual) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                offset = if (scale > 1f) offset + pan else Offset.Zero
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (pageBitmap != null) {
                        Image(
                            bitmap = pageBitmap.asImageBitmap(),
                            contentDescription = "Página PDF",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("No se pudo abrir este PDF.", color = Color.White)
                    }
                }

                if (totalPaginas > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF14171C))
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (paginaActual > 0) {
                                    paginaActual--
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            },
                            enabled = paginaActual > 0
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = Color.White)
                        }
                        Text(
                            text = "${paginaActual + 1} / $totalPaginas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                if (paginaActual < totalPaginas - 1) {
                                    paginaActual++
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                            },
                            enabled = paginaActual < totalPaginas - 1
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoConfirmarAccionArchivo(
    apunte: ApunteGeneral,
    esPropio: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (esPropio) "Borrar archivo" else "Ocultar archivo",
                fontWeight = FontWeight.Bold,
                color = EstudioColors.navy
            )
        },
        text = {
            Text(
                text = if (esPropio) {
                    "¿Seguro que quieres eliminar \"${apunte.titulo}\" para todos? Esta acción no se puede deshacer."
                } else {
                    "¿Quieres ocultar \"${apunte.titulo}\" de tu lista? Dejarás de verlo en Públicos y Favoritos."
                },
                color = EstudioColors.secondaryText
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esPropio) Color(0xFFE05252) else EstudioColors.primary
                )
            ) {
                Text(if (esPropio) "Borrar" else "Ocultar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = EstudioColors.secondaryText)
            }
        }
    )
}