package com.example.estudiamentes.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.estudiamentes.model.SalaModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SearchSalaScreen(
    onBack: () -> Unit,
    onSalaUnida: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var salasEncontradas by remember { mutableStateOf<List<SalaModel>>(emptyList()) }
    var mensajeError by remember { mutableStateOf("") }
    var isBuscando by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("salas_unidas", Context.MODE_PRIVATE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF5A9BFF)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Buscar Sala",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5A9BFF)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                mensajeError = ""
            },
            placeholder = { Text("Nombre, etiqueta o código...", color = Color(0xFF8A9EBF)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF1A1F36))
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF5A9BFF),
                unfocusedBorderColor = Color(0xFFD1DDF2),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF5A9BFF)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (searchQuery.isNotBlank()) {
                    isBuscando = true
                    mensajeError = ""
                    val queryText = searchQuery.trim().lowercase()

                    FirebaseFirestore.getInstance()
                        .collection("groups")
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            isBuscando = false
                            val resultados = querySnapshot.documents.mapNotNull { doc ->
                                val sala = doc.toObject(SalaModel::class.java)?.copy(id = doc.id)
                                val nombre = (doc.getString("name") ?: doc.getString("nombre") ?: "").lowercase()
                                val codigo = (doc.getString("codigo") ?: doc.getString("code") ?: "").lowercase()
                                val etiquetas = (doc.get("etiquetas") as? List<*>)?.map { it.toString().lowercase() } ?: emptyList()

                                val coincide = codigo == queryText ||
                                        nombre.contains(queryText) ||
                                        etiquetas.any { it.contains(queryText) }

                                if (sala != null && coincide) sala else null
                            }

                            if (resultados.isNotEmpty()) {
                                salasEncontradas = resultados
                            } else {
                                salasEncontradas = emptyList()
                                mensajeError = "No se ha encontrado ninguna sala con ese criterio."
                            }
                        }
                        .addOnFailureListener {
                            isBuscando = false
                            mensajeError = "Error de conexión al buscar la sala."
                        }
                }
            },
            enabled = !isBuscando && searchQuery.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A9BFF))
        ) {
            if (isBuscando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Buscar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (mensajeError.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = mensajeError, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(salasEncontradas) { sala ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sala.name.ifEmpty { "Sala sin nombre" },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1F36)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (sala.isPrivate) "Sala Privada" else "Sala Pública",
                                fontSize = 12.sp,
                                color = Color(0xFF8A9EBF)
                            )
                        }

                        Button(
                            onClick = {
                                val unidasSet = prefs.getStringSet("joined_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                                unidasSet.add(sala.id)
                                prefs.edit().putStringSet("joined_ids", unidasSet).apply()

                                onSalaUnida(sala.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A9BFF)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Entrar", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}