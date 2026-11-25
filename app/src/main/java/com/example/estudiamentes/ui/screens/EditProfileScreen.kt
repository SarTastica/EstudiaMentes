
package com.example.estudiamentes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val EditBlue = Color(0xFF1E88E5)
private val EditLightBlue = Color(0xFFE2F4FF)

@Composable
fun EditProfileScreen(
    currentUsername: String = "",
    currentPhotoUrl: String? = null,
    currentDailyGoal: Float = 2f,
    onSave: (String, Uri?, Float) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = {}
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var username by remember { mutableStateOf(currentUsername) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var dailyGoal by remember {
        mutableFloatStateOf(currentDailyGoal.coerceIn(1f, 24f))
    }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var savedMessage by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }

    // Cargar los datos actuales del usuario
    LaunchedEffect(uid) {
        if (uid == null) {
            errorMessage = "No hay una sesión iniciada."
            loading = false
            return@LaunchedEffect
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    username = document.getString("username") ?: ""
                    dailyGoal = (document.getLong("horasPorDia")?.toFloat()
                        ?: currentDailyGoal).coerceIn(1f, 24f)
                } else {
                    errorMessage = "No se encontró el perfil."
                }
                loading = false
            }
            .addOnFailureListener {
                errorMessage = "No se pudieron cargar tus datos."
                loading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Editar perfil",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = EditBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(EditLightBlue)
        )

        if (loading) {
            Spacer(modifier = Modifier.height(40.dp))
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = EditBlue
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))

            // Foto de perfil: se mantiene como estaba
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD7EDFF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null || !currentPhotoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUri ?: currentPhotoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Foto predeterminada",
                            modifier = Modifier.size(75.dp),
                            tint = EditBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Cambiar foto",
                    color = EditBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        photoPicker.launch("image/*")
                    }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Nombre de usuario
            Text(
                text = "Nombre de perfil",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    savedMessage = false
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text("Introduce tu nombre")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EditBlue,
                    focusedLabelColor = EditBlue,
                    cursorColor = EditBlue
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Objetivo diario
            Text(
                text = "Horas de estudio al día",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${dailyGoal.toInt()} ${if (dailyGoal.toInt() == 1) "hora" else "horas"}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = EditBlue
            )

            Text(
                text = "Tu objetivo diario de estudio",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Slider(
                value = dailyGoal,
                onValueChange = {
                    dailyGoal = it
                    savedMessage = false
                    errorMessage = null
                },
                valueRange = 1f..24f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = EditBlue,
                    activeTrackColor = EditBlue,
                    inactiveTrackColor = EditLightBlue
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 hora", fontSize = 12.sp, color = Color.Gray)
                Text("24 horas", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Guardar en Firestore
            Button(
                onClick = {
                    if (uid == null) {
                        errorMessage = "No hay una sesión iniciada."
                        return@Button
                    }

                    saving = true
                    errorMessage = null
                    savedMessage = false

                    db.collection("users")
                        .document(uid)
                        .update(
                            mapOf(
                                "username" to username.trim(),
                                "horasPorDia" to dailyGoal.toInt()
                            )
                        )
                        .addOnSuccessListener {
                            saving = false
                            savedMessage = true

                            onSave(
                                username.trim(),
                                photoUri,
                                dailyGoal
                            )
                        }
                        .addOnFailureListener {
                            saving = false
                            errorMessage = "No se pudieron guardar los cambios."
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EditBlue
                ),
                enabled = username.isNotBlank() && !saving
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar cambios",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cancelar",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onCancel() }
                    .padding(8.dp),
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )

            if (savedMessage) {
                Text(
                    text = "Cambios guardados correctamente",
                    color = EditBlue,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
