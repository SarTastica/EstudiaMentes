
package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.example.estudiamentes.data.crearGrupo

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CreateSalaScreen(
    onBack: () -> Unit = {},
    onSalaCreada: (String) -> Unit = {}
) {
    var nombreSala by remember { mutableStateOf("") }
    var codigoGenerado by remember { mutableStateOf<String?>(null) }
    var groupIdGenerado by remember { mutableStateOf<String?>(null) }

    var privacidad by remember { mutableStateOf("privado") }
    var mostrarPopup by remember { mutableStateOf(false) }

    var creandoSala by remember { mutableStateOf(false) }
    var errorCreacion by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ENCABEZADO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF64B5F6)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Crear sala",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF64B5F6)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NOMBRE DE LA SALA
        Text(
            text = "Nombre de la sala",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nombreSala,
            onValueChange = {
                nombreSala = it
                errorCreacion = null
            },
            placeholder = {
                Text("Ej. Grupo de biología")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            enabled = !creandoSala,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF333333),
                unfocusedTextColor = Color(0xFF333333),
                focusedBorderColor = Color(0xFF64B5F6),
                unfocusedBorderColor = Color(0xFFBDBDBD),
                cursorColor = Color(0xFF64B5F6)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PRIVACIDAD
        Text(
            text = "Privacidad de la sala",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF424242)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // OPCIÓN PÚBLICA
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !creandoSala) {
                        privacidad = "publico"
                        errorCreacion = null
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (privacidad == "publico")
                        Color(0xFFE3F2FD)
                    else
                        Color.White
                ),
                border = BorderStroke(
                    2.dp,
                    if (privacidad == "publico")
                        Color(0xFF64B5F6)
                    else
                        Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Público",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Público",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Visible para todos",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // OPCIÓN PRIVADA
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !creandoSala) {
                        privacidad = "privado"
                        errorCreacion = null
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (privacidad == "privado")
                        Color(0xFFE3F2FD)
                    else
                        Color.White
                ),
                border = BorderStroke(
                    2.dp,
                    if (privacidad == "privado")
                        Color(0xFF64B5F6)
                    else
                        Color(0xFFE0E0E0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privado",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(30.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Privado",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Solo con invitación",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // IMAGEN PROVISIONAL DE LA SALA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Imagen de la sala",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Imagen de la sala",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF424242)
                )

                Text(
                    text = "Próximamente podrás personalizarla",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BOTÓN CREAR SALA
        Button(
            onClick = {
                val usuario = FirebaseAuth.getInstance().currentUser

                if (usuario == null) {
                    errorCreacion =
                        "Debes iniciar sesión para crear una sala."
                } else {
                    creandoSala = true
                    errorCreacion = null

                    crearGrupo(
                        nombre = nombreSala.trim(),
                        privado = privacidad == "privado",
                        adminId = usuario.uid,

                        onSuccess = { grupo ->
                            codigoGenerado = grupo.codigo
                            groupIdGenerado = grupo.groupId
                            mostrarPopup = true
                            creandoSala = false
                        },

                        onError = { error ->
                            creandoSala = false
                            errorCreacion = error.localizedMessage
                                ?: "No se pudo crear la sala."
                        }
                    )
                }
            },
            enabled = nombreSala.isNotBlank() && !creandoSala,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64B5F6)
            )
        ) {
            if (creandoSala) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Creando sala...")
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Crear sala",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // MENSAJE DE ERROR
        errorCreacion?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // POPUP DE SALA CREADA
    if (mostrarPopup && codigoGenerado != null) {
        AlertDialog(
            onDismissRequest = {
                mostrarPopup = false
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,

            title = {
                Text(
                    text = "¡Sala creada!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64B5F6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu sala \"$nombreSala\" está lista.",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF424242)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Código de acceso",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // MANTENER PULSADO PARA COPIAR
                    Text(
                        text = codigoGenerado!!,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                clipboardManager.setText(
                                    AnnotatedString(codigoGenerado!!)
                                )
                            }
                        ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF333333),
                        letterSpacing = 6.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Mantén pulsado el código para copiarlo.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Comparte este código con las personas que quieras invitar.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        mostrarPopup = false

                        groupIdGenerado?.let { id ->
                            onSalaCreada(id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64B5F6)
                    )
                ) {
                    Text(
                        text = "Aceptar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}
