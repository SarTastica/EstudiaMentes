package com.example.estudiamentes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SalaOpcionesTab(
    nombreSalaActual: String,
    esPrivadaActual: Boolean,
    codigoInvitacion: String = "",
    capacidadActual: Int,
    esAdmin: Boolean,
    participantesActuales: List<EstudianteEstudio>,
    onConfirmarCambios: (
        nuevoNombre: String,
        nuevaPrivacidad: Boolean,
        nuevaCapacidad: Int,
        nuevaLista: List<EstudianteEstudio>
    ) -> Unit,
    onCerrarSala: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val nombreBase = remember(nombreSalaActual) {
        if (nombreSalaActual == "Cargando sala...") "" else nombreSalaActual
    }

    var nombreEditado by remember(nombreBase) { mutableStateOf(nombreBase) }
    var esPrivada by remember(esPrivadaActual) { mutableStateOf(esPrivadaActual) }
    var capacidadMaxima by remember(capacidadActual) { mutableFloatStateOf(capacidadActual.toFloat()) }
    var participantes by remember(participantesActuales) { mutableStateOf(participantesActuales) }
    var mostrarDialogoCerrar by remember { mutableStateOf(false) }

    val hayCambiosPendientes = nombreEditado.trim() != nombreBase ||
            esPrivada != esPrivadaActual ||
            capacidadMaxima.roundToInt() != capacidadActual ||
            participantes.size != participantesActuales.size

    if (mostrarDialogoCerrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrar = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = if (esAdmin) "¿Cerrar sala definitivamente?" else "¿Salir de la sala?",
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy
                )
            },
            text = {
                Text(
                    text = if (esAdmin) {
                        "Se expulsará a todos los miembros y la sala dejará de estar disponible."
                    } else {
                        "Dejarás de ver el progreso y los apuntes de este grupo."
                    },
                    color = EstudioColors.secondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoCerrar = false
                        onCerrarSala()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE05252))
                ) {
                    Text(if (esAdmin) "Cerrar sala" else "Salir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrar = false }) {
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
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = EstudioColors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajustes de la sala",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EstudioColors.navy
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nombre de la sala",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy
                )
                OutlinedTextField(
                    value = nombreEditado,
                    onValueChange = { nombreEditado = it },
                    placeholder = {
                        Text(
                            text = "Introduce el nombre de la sala",
                            fontSize = 13.sp,
                            color = EstudioColors.secondaryText
                        )
                    },
                    enabled = esAdmin,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EstudioColors.primary,
                        unfocusedBorderColor = EstudioColors.primarySoft,
                        focusedContainerColor = EstudioColors.sky,
                        unfocusedContainerColor = EstudioColors.sky,
                        disabledContainerColor = EstudioColors.background
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EstudioColors.sky,
                border = BorderStroke(1.dp, EstudioColors.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (esPrivada) Icons.Default.Lock else Icons.Default.Public,
                                contentDescription = null,
                                tint = EstudioColors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (esPrivada) "Sala privada" else "Sala pública",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EstudioColors.navy
                                )
                                Text(
                                    text = if (esPrivada) {
                                        "Solo se puede entrar mediante código"
                                    } else {
                                        "Visible para cualquier estudiante"
                                    },
                                    fontSize = 11.sp,
                                    color = EstudioColors.secondaryText
                                )
                            }
                        }

                        Switch(
                            checked = esPrivada,
                            onCheckedChange = { if (esAdmin) esPrivada = it },
                            enabled = esAdmin,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EstudioColors.primary
                            )
                        )
                    }

                    if (esPrivada) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EstudioColors.white,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Código de invitación:",
                                    fontSize = 12.sp,
                                    color = EstudioColors.secondaryText
                                )
                                Text(
                                    text = codigoInvitacion.ifBlank { "Sin código" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EstudioColors.primary
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = EstudioColors.sky,
                border = BorderStroke(1.dp, EstudioColors.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Capacidad máxima",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )
                        Surface(
                            shape = CircleShape,
                            color = EstudioColors.primarySoft
                        ) {
                            Text(
                                text = "${capacidadMaxima.roundToInt()} / 8 usuarios",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EstudioColors.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val minCapacidad = participantes.size.coerceIn(2, 8).toFloat()

                    Slider(
                        value = capacidadMaxima.coerceAtLeast(minCapacidad),
                        onValueChange = { if (esAdmin) capacidadMaxima = it },
                        valueRange = 2f..8f,
                        steps = 5,
                        enabled = esAdmin,
                        colors = SliderDefaults.colors(
                            thumbColor = EstudioColors.primary,
                            activeTrackColor = EstudioColors.primary,
                            inactiveTrackColor = EstudioColors.primarySoft
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Participantes (${participantes.size}/${capacidadMaxima.roundToInt()})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy
                )

                if (participantes.isEmpty()) {
                    Text(
                        text = "No hay participantes cargados todavía.",
                        fontSize = 12.sp,
                        color = EstudioColors.secondaryText
                    )
                } else {
                    participantes.forEach { miembro ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = EstudioColors.background,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(miembro.colorAvatar, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = miembro.nombre.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (miembro.isCurrentUser) "${miembro.nombre} (Tú)" else miembro.nombre,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EstudioColors.navy
                                    )
                                    if (miembro.isCurrentUser && esAdmin) {
                                        Text(
                                            text = "Administrador",
                                            fontSize = 11.sp,
                                            color = EstudioColors.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (esAdmin && !miembro.isCurrentUser) {
                                    IconButton(
                                        onClick = {
                                            participantes = participantes.filter { it.uid != miembro.uid }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonRemove,
                                            contentDescription = "Expulsar a ${miembro.nombre}",
                                            tint = Color(0xFFE05252),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (esAdmin) {
                Button(
                    onClick = {
                        val nombreFinal = nombreEditado.trim().ifEmpty { nombreBase }
                        onConfirmarCambios(
                            nombreFinal,
                            esPrivada,
                            capacidadMaxima.roundToInt(),
                            participantes
                        )
                        Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                    },
                    enabled = hayCambiosPendientes && nombreEditado.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EstudioColors.primary,
                        disabledContainerColor = EstudioColors.primarySoft
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (hayCambiosPendientes) Color.White else EstudioColors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmar cambios",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hayCambiosPendientes) Color.White else EstudioColors.secondaryText
                    )
                }
            }

            Button(
                onClick = { mostrarDialogoCerrar = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFDECEC),
                    contentColor = Color(0xFFE05252)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = if (esAdmin) Icons.Default.DeleteForever else Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (esAdmin) "Cerrar sala" else "Salir de la sala",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}