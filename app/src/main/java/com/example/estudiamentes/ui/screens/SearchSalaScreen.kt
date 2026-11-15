package com.example.estudiamentes.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.data.buscarGrupoPorCodigo
import com.example.estudiamentes.data.unirseAGrupo
import com.example.estudiamentes.model.SalaModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.Normalizer

private fun String.normalizarBusqueda(): String {
    val sinAcentos = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return sinAcentos.lowercase().trim()
}

@Composable
fun SearchSalaScreen(
    onBack: () -> Unit = {},
    onSalaUnida: (String) -> Unit = {},
    onEntrarSala: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val prefsUnidas = remember { context.getSharedPreferences("salas_unidas", Context.MODE_PRIVATE) }

    var queryBusqueda by remember { mutableStateOf("") }
    var codigoSalaPrivada by remember { mutableStateOf("") }
    var salasPublicas by remember { mutableStateOf<List<SalaModel>>(emptyList()) }
    var cargandoSalas by remember { mutableStateOf(true) }
    var procesandoUnion by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = db.collection("groups")
            .addSnapshotListener { snapshot, error ->
                cargandoSalas = false
                if (error != null || snapshot == null) return@addSnapshotListener

                salasPublicas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SalaModel::class.java)?.copy(id = doc.id)
                }.filter { !it.privado }
            }

        onDispose { listener.remove() }
    }

    val salasFiltradas = remember(queryBusqueda, salasPublicas) {
        val filtro = queryBusqueda.normalizarBusqueda()
        if (filtro.isEmpty()) {
            salasPublicas
        } else {
            salasPublicas.filter { sala ->
                sala.name.normalizarBusqueda().contains(filtro) ||
                        sala.etiquetas.any { it.normalizarBusqueda().contains(filtro) }
            }
        }
    }

    fun registrarYEntrar(groupId: String) {
        val actuales = prefsUnidas.getStringSet("joined_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        actuales.add(groupId)
        prefsUnidas.edit().putStringSet("joined_ids", actuales).apply()
        onSalaUnida(groupId)
        onEntrarSala(groupId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = BluePrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Buscar sala",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unirse a sala privada",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = codigoSalaPrivada,
                        onValueChange = { nuevo ->
                            if (nuevo.length <= 6 && nuevo.all { it.isDigit() }) {
                                codigoSalaPrivada = nuevo
                            }
                        },
                        placeholder = { Text("Código de 6 dígitos", fontSize = 13.sp, color = TextGray) },
                        singleLine = true,
                        enabled = !procesandoUnion,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            unfocusedBorderColor = Color(0xFFD1DDF2)
                        )
                    )

                    Button(
                        onClick = {
                            if (procesandoUnion) return@Button
                            procesandoUnion = true
                            buscarGrupoPorCodigo(
                                codigo = codigoSalaPrivada,
                                onSuccess = { grupo ->
                                    unirseAGrupo(
                                        groupId = grupo.groupId,
                                        onSuccess = {
                                            procesandoUnion = false
                                            Toast.makeText(
                                                context,
                                                "¡Te has unido a ${grupo.nombre}!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            registrarYEntrar(grupo.groupId)
                                        },
                                        onError = { error ->
                                            procesandoUnion = false
                                            Toast.makeText(
                                                context,
                                                error.message ?: "No se pudo unir a la sala",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                },
                                onError = { error ->
                                    procesandoUnion = false
                                    Toast.makeText(
                                        context,
                                        error.message ?: "Código no válido",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        },
                        enabled = codigoSalaPrivada.length == 6 && !procesandoUnion,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("Unirse", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Buscar salas públicas por etiqueta o código",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = queryBusqueda,
            onValueChange = { queryBusqueda = it },
            placeholder = { Text("Ej. 40980, Informática Gráfica, Química...", color = TextGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextDark) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = Color(0xFFD1DDF2),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            cargandoSalas -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            salasFiltradas.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron salas públicas con esa etiqueta.",
                        color = TextGray,
                        fontSize = 15.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(salasFiltradas, key = { it.id }) { sala ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(BlueLight, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = BluePrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sala.name.ifEmpty { "Sala sin nombre" },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    val tagsTexto = if (sala.etiquetas.isNotEmpty()) {
                                        sala.etiquetas.joinToString(" • ")
                                    } else {
                                        "General"
                                    }
                                    val ocupacion = "${sala.memberIds.size.coerceAtLeast(1)}/${if (sala.maxMembers > 0) sala.maxMembers else 8}"
                                    Text(
                                        text = "$tagsTexto  ($ocupacion)",
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (procesandoUnion) return@Button
                                        procesandoUnion = true
                                        unirseAGrupo(
                                            groupId = sala.id,
                                            onSuccess = {
                                                procesandoUnion = false
                                                registrarYEntrar(sala.id)
                                            },
                                            onError = { error ->
                                                procesandoUnion = false
                                                Toast.makeText(
                                                    context,
                                                    error.message ?: "No se pudo entrar",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    },
                                    enabled = !procesandoUnion,
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Entrar", fontSize = 13.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}