package com.example.estudiamentes.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.data.limpiarSalasInactivas
import com.example.estudiamentes.data.unirseAGrupo
import com.example.estudiamentes.model.SalaModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val BluePrimary = Color(0xFF5A9BFF)
val BlueLight = Color(0xFFE8F1FF)
val TextGray = Color(0xFF8A9EBF)
val TextDark = Color(0xFF1A1F36)

@Composable
fun SalasScreen(
    paddingValues: PaddingValues,
    onBuscarSala: () -> Unit,
    onCrearSala: () -> Unit,
    onEntrarSala: (String) -> Unit
) {
    var salasList by remember { mutableStateOf<List<SalaModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isJoining by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val prefsSilenciadas = remember { context.getSharedPreferences("salas_silenciadas", Context.MODE_PRIVATE) }
    val prefsUnidas = remember { context.getSharedPreferences("salas_unidas", Context.MODE_PRIVATE) }

    var hiddenSalas by remember {
        mutableStateOf(prefsSilenciadas.getStringSet("hidden_ids", emptySet()) ?: emptySet())
    }
    var joinedSalas by remember {
        mutableStateOf(prefsUnidas.getStringSet("joined_ids", emptySet()) ?: emptySet())
    }

    DisposableEffect(Unit) {
        limpiarSalasInactivas()

        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("groups")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null || snapshot == null) return@addSnapshotListener

                salasList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SalaModel::class.java)?.copy(id = doc.id)
                }
            }

        onDispose { listener.remove() }
    }

    val visibleSalas = salasList.filter { sala ->
        val esPrivado = sala.privado
        val estaUnidaLocal = sala.id in joinedSalas
        val esMiembroEnBD = currentUserId.isNotBlank() && (
                sala.memberIds.contains(currentUserId) || sala.adminId == currentUserId
                )
        val estaOculta = sala.id in hiddenSalas

        (!esPrivado || estaUnidaLocal || esMiembroEnBD) && !estaOculta
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salas",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary
            )

            IconButton(
                onClick = onCrearSala,
                modifier = Modifier
                    .size(48.dp)
                    .background(BlueLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clickable { onBuscarSala() }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar por asignatura o código...", color = TextGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextDark)
                },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color(0xFFD1DDF2),
                    disabledContainerColor = Color.White,
                    disabledPlaceholderColor = TextGray
                ),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            visibleSalas.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay salas disponibles.",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(visibleSalas, key = { it.id.ifBlank { it.name } }) { sala ->
                        SalaCardItem(
                            sala = sala,
                            isJoining = isJoining,
                            onEntrarClick = {
                                val routeId = sala.id.ifBlank { sala.name }
                                if (routeId.isBlank() || isJoining) return@SalaCardItem

                                if (sala.id.isNotBlank()) {
                                    isJoining = true
                                    unirseAGrupo(
                                        groupId = sala.id,
                                        onSuccess = {
                                            isJoining = false
                                            val updatedJoined = joinedSalas.toMutableSet().apply { add(sala.id) }
                                            prefsUnidas.edit().putStringSet("joined_ids", updatedJoined).apply()
                                            joinedSalas = updatedJoined
                                            onEntrarSala(routeId)
                                        },
                                        onError = { error ->
                                            isJoining = false
                                            Toast.makeText(
                                                context,
                                                error.message ?: "No se pudo entrar a la sala",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                } else {
                                    onEntrarSala(routeId)
                                }
                            },
                            onSilenciarClick = {
                                if (sala.id.isNotBlank()) {
                                    val updatedSet = hiddenSalas.toMutableSet().apply { add(sala.id) }
                                    prefsSilenciadas.edit().putStringSet("hidden_ids", updatedSet).apply()
                                    hiddenSalas = updatedSet
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
fun SalaCardItem(
    sala: SalaModel,
    isJoining: Boolean = false,
    onEntrarClick: () -> Unit,
    onSilenciarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(BluePrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (sala.privado) Icons.Default.Lock else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sala.name.ifEmpty { "Grupo sin nombre" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = if (sala.privado) "Sala Privada" else "Asignatura general",
                        fontSize = 12.sp,
                        color = BluePrimary
                    )
                }

                IconButton(onClick = onSilenciarClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Silenciar sala",
                        tint = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sala.etiquetas.isNotEmpty()) {
                        sala.etiquetas.forEach { TagItem(it) }
                    } else {
                        TagItem("General")
                    }
                    val totalMiembros = sala.memberIds.size.coerceAtLeast(1)
                    val maxMiembros = if (sala.maxMembers > 0) sala.maxMembers else 8
                    TagItem("$totalMiembros/$maxMiembros")
                }

                Button(
                    onClick = onEntrarClick,
                    enabled = !isJoining,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Entrar", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TagItem(text: String) {
    Box(
        modifier = Modifier
            .background(BlueLight, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = BluePrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}