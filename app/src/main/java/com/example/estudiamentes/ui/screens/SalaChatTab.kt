package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class MensajeSala(
    val id: String = "",
    val texto: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val timestamp: Long = 0L
)

@Composable
fun SalaChatTab(
    groupId: String,
    nombreSala: String,
    modifier: Modifier = Modifier
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    var mensajes by remember { mutableStateOf<List<MensajeSala>>(emptyList()) }
    var textoMensaje by remember { mutableStateOf("") }
    var nombreUsuarioActual by remember { mutableStateOf("Tú") }
    val listState = rememberLazyListState()

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    nombreUsuarioActual = doc.getString("name")
                        ?: doc.getString("username")
                                ?: currentUser.displayName
                                ?: currentUser.email?.substringBefore("@")
                                ?: "Tú"
                }
        }
    }

    DisposableEffect(groupId) {
        if (groupId.isBlank()) return@DisposableEffect onDispose {}

        val listener = db.collection("groups")
            .document(groupId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                mensajes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(MensajeSala::class.java)?.copy(id = doc.id)
                }
            }

        onDispose { listener.remove() }
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
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
                text = "Chat $nombreSala",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = EstudioColors.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                color = EstudioColors.primary.copy(alpha = 0.25f),
                thickness = 1.dp
            )

            if (mensajes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "👋", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Aún no hay mensajes!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )
                        Text(
                            text = "Escribe algo para empezar la conversación.",
                            fontSize = 12.sp,
                            color = EstudioColors.secondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(mensajes, key = { it.id }) { mensaje ->
                        val esMio = mensaje.senderId == currentUser?.uid
                        BurbujaMensajeItem(
                            mensaje = mensaje,
                            esMio = esMio
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = textoMensaje,
                onValueChange = { textoMensaje = it },
                placeholder = {
                    Text(
                        text = "Escribe un mensaje...",
                        fontSize = 13.sp,
                        color = EstudioColors.secondaryText
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val limpio = textoMensaje.trim()
                            if (limpio.isNotEmpty() && groupId.isNotBlank()) {
                                val nuevoMensaje = hashMapOf(
                                    "texto" to limpio,
                                    "senderId" to (currentUser?.uid ?: "anonimo"),
                                    "senderName" to nombreUsuarioActual,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                textoMensaje = ""
                                db.collection("groups")
                                    .document(groupId)
                                    .collection("messages")
                                    .add(nuevoMensaje)
                            }
                        },
                        enabled = textoMensaje.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar mensaje",
                            tint = if (textoMensaje.isNotBlank()) {
                                EstudioColors.primary
                            } else {
                                EstudioColors.primary.copy(alpha = 0.4f)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EstudioColors.primary,
                    unfocusedBorderColor = EstudioColors.primary.copy(alpha = 0.35f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    cursorColor = EstudioColors.primary
                )
            )
        }
    }
}

@Composable
private fun BurbujaMensajeItem(
    mensaje: MensajeSala,
    esMio: Boolean
) {
    val avatarColor = remember(mensaje.senderName) {
        val colores = listOf(
            Color(0xFF5A9BFF),
            Color(0xFF34A853),
            Color(0xFF26616E),
            Color(0xFF83B96D),
            Color(0xFFA99ACD)
        )
        val index = kotlin.math.abs(mensaje.senderName.hashCode()) % colores.size
        colores[index]
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!esMio) {
            AvatarChatCircle(
                nombre = mensaje.senderName.ifBlank { "?" },
                color = avatarColor
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (esMio) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 240.dp)
        ) {
            if (!esMio && mensaje.senderName.isNotBlank()) {
                Text(
                    text = mensaje.senderName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EstudioColors.secondaryText,
                    modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFDCEBFF),
                border = BorderStroke(1.dp, EstudioColors.primary.copy(alpha = 0.25f)),
                modifier = Modifier.padding(top = if (esMio) 12.dp else 0.dp)
            ) {
                Text(
                    text = mensaje.texto,
                    fontSize = 13.sp,
                    color = EstudioColors.navy,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                )
            }
        }

        if (esMio) {
            Spacer(modifier = Modifier.width(8.dp))
            AvatarChatCircle(
                nombre = mensaje.senderName.ifBlank { "T" },
                color = EstudioColors.primary
            )
        }
    }
}

@Composable
private fun AvatarChatCircle(
    nombre: String,
    color: Color
) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = nombre.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}