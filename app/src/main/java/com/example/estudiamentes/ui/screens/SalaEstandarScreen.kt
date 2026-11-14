package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EstudianteEstudio(
    val nombre: String,
    val horas: Float,
    val colorAvatar: Color,
    val avatarResId: Int? = null
)

internal object EstudioColors {
    val background = Color(0xFFF7FAFE)
    val white = Color(0xFFFFFFFF)
    val primary = Color(0xFF5A9BFF)
    val primarySoft = Color(0xFFE7F1FF)
    val navy = Color(0xFF263449)
    val secondaryText = Color(0xFF8291A6)
    val sky = Color(0xFFF3F8FE)
    val meadow = Color(0xFFEAF5E8)
    val trunk = Color(0xFF806B58)
    val apple = Color(0xFFFF5252)
    val appleHighlight = Color(0xFFFFCDD2)
}

@Composable
fun SalaEstandarScreen(
    groupId: String = "",
    nombreSala: String,
    esAdmin: Boolean = false,
    onBack: () -> Unit = {},
    onClasificacionClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onArchivosClick: () -> Unit = {},
    onOpcionesClick: () -> Unit = {}
) {
    // 0 = Chat, 1 = Ranking, 2 = Apuntes, 3 = Ajustes
    var selectedTab by remember { mutableIntStateOf(1) }

    val estadoInicial = remember(groupId) {
        SalasEstadoLocal.obtenerEstado(groupId, nombreSala)
    }

    var tituloSala by remember { mutableStateOf(estadoInicial.nombre) }
    var esSalaPrivada by remember { mutableStateOf(estadoInicial.esPrivada) }
    var capacidadMaxima by remember { mutableIntStateOf(estadoInicial.capacidadMaxima) }
    var listaEstudiantes by remember { mutableStateOf(estadoInicial.estudiantes) }

    // Si el nombre real llega un instante después desde Firebase, actualizamos si aún estaba cargando
    LaunchedEffect(nombreSala) {
        if (nombreSala.isNotBlank() &&
            nombreSala != "Cargando sala..." &&
            (tituloSala.isBlank() || tituloSala == "Cargando sala...")
        ) {
            tituloSala = nombreSala
            SalasEstadoLocal.guardarCambios(
                groupId = groupId,
                nombreInicial = nombreSala,
                nuevoNombre = nombreSala,
                nuevaPrivacidad = esSalaPrivada,
                nuevaCapacidad = capacidadMaxima,
                nuevaLista = listaEstudiantes
            )
        }
    }

    Scaffold(
        containerColor = EstudioColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EstudioColors.background)
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = EstudioColors.navy
                    )
                }

                Text(
                    text = tituloSala,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy
                )

                Surface(
                    shape = CircleShape,
                    color = if (selectedTab == 3) EstudioColors.primarySoft else Color.Transparent
                ) {
                    IconButton(
                        onClick = {
                            selectedTab = if (selectedTab == 3) 1 else 3
                            onOpcionesClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes de la sala",
                            tint = EstudioColors.primary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            }

            Text(
                text = "Tu espacio de estudio",
                fontSize = 14.sp,
                color = EstudioColors.secondaryText,
                modifier = Modifier.padding(start = 12.dp, top = 1.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = EstudioColors.white,
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTabItem(
                        icon = Icons.AutoMirrored.Filled.Message,
                        label = "Chat",
                        isSelected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            onChatClick()
                        }
                    )
                    NavTabItem(
                        icon = Icons.Default.Leaderboard,
                        label = "Ranking",
                        isSelected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onClasificacionClick()
                        }
                    )
                    NavTabItem(
                        icon = Icons.Default.Folder,
                        label = "Apuntes",
                        isSelected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            onArchivosClick()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val tabModifier = Modifier
                .fillMaxWidth()
                .weight(1f)

            when (selectedTab) {
                0 -> SalaChatTab(
                    groupId = groupId,
                    nombreSala = tituloSala,
                    modifier = tabModifier
                )
                1 -> SalaRankingTab(
                    estudiantes = listaEstudiantes,
                    modifier = tabModifier
                )
                2 -> SalaApuntesTab(
                    groupId = groupId,
                    nombreSala = tituloSala,
                    modifier = tabModifier
                )
                3 -> SalaOpcionesTab(
                    nombreSalaActual = tituloSala,
                    esPrivadaActual = esSalaPrivada,
                    capacidadActual = capacidadMaxima,
                    esAdmin = esAdmin,
                    participantesActuales = listaEstudiantes,
                    onConfirmarCambios = { nuevoNombre, nuevaPrivacidad, nuevaCapacidad, nuevaLista ->
                        SalasEstadoLocal.guardarCambios(
                            groupId = groupId,
                            nombreInicial = nombreSala,
                            nuevoNombre = nuevoNombre,
                            nuevaPrivacidad = nuevaPrivacidad,
                            nuevaCapacidad = nuevaCapacidad,
                            nuevaLista = nuevaLista
                        )
                        tituloSala = nuevoNombre
                        esSalaPrivada = nuevaPrivacidad
                        capacidadMaxima = nuevaCapacidad
                        listaEstudiantes = nuevaLista.sortedByDescending { it.horas }
                        selectedTab = 1
                    },
                    onCerrarSala = onBack,
                    modifier = tabModifier
                )
            }
        }
    }
}

@Composable
fun NavTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) EstudioColors.primarySoft else Color.Transparent
    val contentColor =
        if (isSelected) EstudioColors.primary else EstudioColors.secondaryText

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}