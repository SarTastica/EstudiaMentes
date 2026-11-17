package com.example.estudiamentes.ui.setup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val EditBlue = Color(0xFF1E88E5)
private val EditLightBlue = Color(0xFFE2F4FF)

//TODO conectarlo con el firebase para aplicar cambios
@Composable
fun EditProfileScreen(
    currentUsername: String = "",
    currentPhotoUrl: String? = null,
    currentDailyGoal: Float = 2f,
    onSave: (String, Uri?, Float) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = {}
) {
    var username by remember {
        mutableStateOf(currentUsername)
    }

    var photoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var dailyGoal by remember {
        mutableFloatStateOf(currentDailyGoal.coerceIn(1f, 24f))
    }

    var showSavedMessage by remember {
        mutableStateOf(false)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {

        // Título
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

        Spacer(modifier = Modifier.height(32.dp))

        // Foto de perfil
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
                showSavedMessage = false
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
            text = "Horas de estudio diarias",
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

        Slider(
            value = dailyGoal,
            onValueChange = {
                dailyGoal = it
                showSavedMessage = false
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
            Text(
                text = "1 hora",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = "24 horas máximo",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(70.dp))

        // Guardar cambios
        Button(
            onClick = {
                onSave(username.trim(), photoUri, dailyGoal)
                showSavedMessage = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EditBlue
            ),
            enabled = username.isNotBlank()
        ) {
            Text(
                text = "Guardar cambios",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
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

        if (showSavedMessage) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cambios preparados correctamente",
                color = EditBlue,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
