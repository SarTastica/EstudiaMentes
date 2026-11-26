package com.example.estudiamentes.ui.archivos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.ui.screens.EstudioColors
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ArchivosScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val context = LocalContext.current
    val repository = remember { ArchivosRepository() }
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val uid = currentUser?.uid

    var todosLosApuntes by remember { mutableStateOf<List<ApunteGeneral>>(emptyList()) }
    var etiquetasDisponibles by remember { mutableStateOf<List<String>>(emptyList()) }
    var idsFavoritos by remember { mutableStateOf<Set<String>>(emptySet()) }
    var idsOcultos by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isUploading by remember { mutableStateOf(false) }
    var nombreUsuarioActual by remember { mutableStateOf("Estudiante") }

    var tabSeleccionada by remember { mutableIntStateOf(0) }
    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogoSubida by remember { mutableStateOf(false) }
    var fotoFallbackSeleccionada by remember { mutableStateOf<ApunteGeneral?>(null) }
    var pdfFallbackSeleccionado by remember { mutableStateOf<ApunteGeneral?>(null) }
    var apunteParaAccion by remember { mutableStateOf<ApunteGeneral?>(null) }

    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose {}
        val fallback = currentUser.displayName
            ?: currentUser.email?.substringBefore("@")
            ?: "Estudiante"
        val listener = repository.observarUsuarioYFavoritos(uid, fallback) { nombre, favs, ocultos ->
            nombreUsuarioActual = nombre
            idsFavoritos = favs
            idsOcultos = ocultos
        }
        onDispose { listener.remove() }
    }

    DisposableEffect(Unit) {
        val listenerApuntes = repository.observarApuntes { lista ->
            todosLosApuntes = lista
        }
        val listenerEtiquetas = repository.observarEtiquetas { tags ->
            etiquetasDisponibles = tags
        }
        onDispose {
            listenerApuntes.remove()
            listenerEtiquetas.remove()
        }
    }

    val etiquetasFrecuentes = remember(todosLosApuntes, etiquetasDisponibles, uid) {
        val propias = todosLosApuntes
            .filter { it.autorId == uid && it.etiqueta.isNotBlank() }
            .groupingBy { it.etiqueta.trim() }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }

        val globales = todosLosApuntes
            .filter { it.etiqueta.isNotBlank() }
            .groupingBy { it.etiqueta.trim() }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }

        (propias + globales + etiquetasDisponibles)
            .distinctBy { it.lowercase() }
            .take(5)
    }

    val apuntesFiltrados = remember(todosLosApuntes, tabSeleccionada, textoBusqueda, idsFavoritos, idsOcultos, uid) {
        val visibles = todosLosApuntes.filter { !idsOcultos.contains(it.id) }
        val baseList = when (tabSeleccionada) {
            0 -> visibles.filter { it.esPublico }
            1 -> visibles.filter { it.autorId == uid }
            2 -> visibles.filter {
                idsFavoritos.contains(it.id) && (it.esPublico || it.autorId == uid)
            }
            else -> emptyList()
        }
        if (textoBusqueda.isBlank()) {
            baseList
        } else {
            val query = textoBusqueda.trim().lowercase()
            baseList.filter {
                it.titulo.lowercase().contains(query) ||
                        it.autorNombre.lowercase().contains(query) ||
                        it.categoria.lowercase().contains(query) ||
                        it.etiqueta.lowercase().contains(query) ||
                        it.descripcion.lowercase().contains(query)
            }
        }
    }

    if (mostrarDialogoSubida) {
        DialogoSubirArchivo(
            etiquetasDisponibles = etiquetasDisponibles,
            etiquetasFrecuentes = etiquetasFrecuentes,
            onDismiss = { mostrarDialogoSubida = false },
            onGuardar = { titulo, descripcion, categoria, etiqueta, tipo, esPublico, url, base64Data ->
                mostrarDialogoSubida = false
                if (uid == null) {
                    Toast.makeText(context, "Debes iniciar sesión para subir archivos", Toast.LENGTH_SHORT).show()
                    return@DialogoSubirArchivo
                }
                isUploading = true
                repository.subirApunte(
                    uid = uid,
                    autorNombre = nombreUsuarioActual,
                    titulo = titulo,
                    descripcion = descripcion,
                    categoria = categoria,
                    etiqueta = etiqueta,
                    tipo = tipo,
                    esPublico = esPublico,
                    url = url,
                    base64Data = base64Data,
                    onSuccess = {
                        isUploading = false
                        val visibilidad = if (esPublico) "público" else "privado"
                        Toast.makeText(context, "Apunte $visibilidad guardado", Toast.LENGTH_SHORT).show()
                    },
                    onError = { msg ->
                        isUploading = false
                        Toast.makeText(context, "Error: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    if (fotoFallbackSeleccionada != null) {
        DialogoPrevisualizarFoto(
            apunte = fotoFallbackSeleccionada!!,
            onDismiss = { fotoFallbackSeleccionada = null }
        )
    }

    if (pdfFallbackSeleccionado != null) {
        DialogoVisorPdfPantallaCompleta(
            apunte = pdfFallbackSeleccionado!!,
            onDismiss = { pdfFallbackSeleccionado = null }
        )
    }

    if (apunteParaAccion != null) {
        val objetivo = apunteParaAccion!!
        val esPropio = objetivo.autorId == uid
        DialogoConfirmarAccionArchivo(
            apunte = objetivo,
            esPropio = esPropio,
            onDismiss = { apunteParaAccion = null },
            onConfirm = {
                apunteParaAccion = null
                if (uid != null) {
                    if (esPropio) {
                        repository.borrarApunte(
                            uid = uid,
                            apunteId = objetivo.id,
                            onSuccess = {
                                Toast.makeText(context, "Archivo borrado", Toast.LENGTH_SHORT).show()
                            },
                            onError = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        repository.ocultarApunte(uid = uid, apunteId = objetivo.id) {
                            Toast.makeText(context, "Archivo ocultado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Archivos y Apuntes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EstudioColors.primary
                )
                if (idsOcultos.isNotEmpty() && uid != null) {
                    TextButton(
                        onClick = {
                            repository.restaurarOcultos(uid) {
                                Toast.makeText(context, "Archivos ocultos restaurados", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text(
                            text = "Ver ocultos (${idsOcultos.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar por título, asignatura, categoría...", fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = EstudioColors.primary)
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = EstudioColors.primary,
                    unfocusedBorderColor = EstudioColors.sky
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            val titulosTabs = listOf("Públicos", "Mis archivos", "Favoritos")
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                titulosTabs.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = titulosTabs.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = EstudioColors.primary,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.White,
                            inactiveContentColor = EstudioColors.navy
                        )
                    ) {
                        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (isUploading) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = EstudioColors.primary,
                    trackColor = EstudioColors.primarySoft
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (apuntesFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (tabSeleccionada == 2) "⭐" else "📂",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (tabSeleccionada) {
                                0 -> "No se encontraron apuntes públicos"
                                1 -> "Aún no has subido archivos"
                                else -> "No tienes apuntes en favoritos"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 84.dp)
                ) {
                    items(apuntesFiltrados, key = { it.id }) { apunte ->
                        ArchivoItemCard(
                            apunte = apunte,
                            esPropio = apunte.autorId == uid,
                            esFavorito = idsFavoritos.contains(apunte.id),
                            mostrarBadgePrivacidad = tabSeleccionada == 1,
                            onToggleFavorito = {
                                if (uid != null) {
                                    repository.toggleFavorito(uid, apunte.id, idsFavoritos.contains(apunte.id))
                                }
                            },
                            onBorrarOOcultar = { apunteParaAccion = apunte },
                            onClick = {
                                repository.abrirApunteConSistema(
                                    context = context,
                                    apunte = apunte,
                                    onFallbackFoto = { fotoFallbackSeleccionada = it },
                                    onFallbackPdf = { pdfFallbackSeleccionado = it }
                                )
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarDialogoSubida = true },
            containerColor = EstudioColors.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Subir archivo"
            )
        }
    }
}