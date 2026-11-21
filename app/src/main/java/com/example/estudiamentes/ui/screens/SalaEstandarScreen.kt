package com.example.estudiamentes.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.data.eliminarSalaCompleta
import com.example.estudiamentes.data.registrarActividadGrupo
import com.example.estudiamentes.data.salirDeGrupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class EstudianteEstudio(
    val uid: String,
    val nombre: String,
    val horas: Float,
    val colorAvatar: Color,
    val avatarResId: Int? = null,
    val isCurrentUser: Boolean = false
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
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUserId = auth.currentUser?.uid ?: ""
    val prefsUnidas = remember { context.getSharedPreferences("salas_unidas", Context.MODE_PRIVATE) }

    var selectedTab by remember { mutableIntStateOf(1) }

    var tituloSala by remember(nombreSala) { mutableStateOf(nombreSala) }
    var esSalaPrivada by remember { mutableStateOf(false) }
    var codigoInvitacion by remember { mutableStateOf("") }
    var capacidadMaxima by remember { mutableIntStateOf(8) }
    var esAdminReal by remember(esAdmin) { mutableStateOf(esAdmin) }
    var listaEstudiantes by remember { mutableStateOf<List<EstudianteEstudio>>(emptyList()) }
    var cargandoMiembros by remember { mutableStateOf(true) }
    var salaCargadaAlgunaVez by remember { mutableStateOf(false) }

    fun quitarSalaDeLocal() {
        if (groupId.isBlank()) return
        val actuales = prefsUnidas.getStringSet("joined_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        actuales.remove(groupId)
        prefsUnidas.edit().putStringSet("joined_ids", actuales).apply()
    }

    DisposableEffect(groupId) {
        if (groupId.isBlank()) {
            cargandoMiembros = false
            return@DisposableEffect onDispose {}
        }

        registrarActividadGrupo(groupId)

        val groupRef = db.collection("groups").document(groupId)

        val groupListener = groupRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            if (!snapshot.exists()) {
                if (salaCargadaAlgunaVez) {
                    quitarSalaDeLocal()
                    Toast.makeText(context, "La sala ha sido cerrada", Toast.LENGTH_SHORT).show()
                    onBack()
                }
                return@addSnapshotListener
            }

            salaCargadaAlgunaVez = true

            val nombreBD = snapshot.getString("name")
            if (!nombreBD.isNullOrBlank()) {
                tituloSala = nombreBD
            }
            esSalaPrivada = snapshot.getBoolean("privado")
                ?: snapshot.getBoolean("isPrivate")
                        ?: false
            codigoInvitacion = snapshot.getString("codigo") ?: ""
            capacidadMaxima = (snapshot.getLong("maxMembers") ?: 8L).toInt()

            val adminId = snapshot.getString("adminId") ?: ""
            if (adminId.isNotBlank()) {
                esAdminReal = (adminId == currentUserId)
            }
        }

        val coloresPaleta = listOf(
            Color(0xFF5A9BFF),
            Color(0xFF83B96D),
            Color(0xFFE99A9A),
            Color(0xFFA99ACD),
            Color(0xFFE8BD62),
            Color(0xFF4DB6AC),
            Color(0xFFFF8A65),
            Color(0xFF7986CB)
        )

        val membersListener = groupRef.collection("members")
            .addSnapshotListener { snapshot, error ->
                cargandoMiembros = false
                if (error != null || snapshot == null) return@addSnapshotListener

                val miembros = snapshot.documents.mapIndexed { index, doc ->
                    val uid = doc.getString("uid") ?: doc.id
                    val nombre = doc.getString("name") ?: "Estudiante"
                    val horas = (doc.getDouble("studyHours") ?: 0.0).toFloat()

                    EstudianteEstudio(
                        uid = uid,
                        nombre = nombre,
                        horas = horas,
                        colorAvatar = coloresPaleta[index % coloresPaleta.size],
                        isCurrentUser = uid == currentUserId
                    )
                }.sortedByDescending { it.horas }

                listaEstudiantes = miembros
            }

        onDispose {
            groupListener.remove()
            membersListener.remove()
        }
    }

    Scaffold(
        containerColor = EstudioColors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(EstudioColors.background)
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
                    isLoading = cargandoMiembros,
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
                    codigoInvitacion = codigoInvitacion,
                    capacidadActual = capacidadMaxima,
                    esAdmin = esAdminReal,
                    participantesActuales = listaEstudiantes,
                    onConfirmarCambios = { nuevoNombre, nuevaPrivacidad, nuevaCapacidad, nuevaLista ->
                        if (groupId.isNotBlank()) {
                            val groupRef = db.collection("groups").document(groupId)

                            val expulsados = listaEstudiantes.filter { anterior ->
                                nuevaLista.none { actual -> actual.uid == anterior.uid }
                            }
                            expulsados.forEach { expulsado ->
                                groupRef.collection("members").document(expulsado.uid).delete()
                                groupRef.update("memberIds", FieldValue.arrayRemove(expulsado.uid))
                            }

                            groupRef.update(
                                mapOf(
                                    "name" to nuevoNombre,
                                    "privado" to nuevaPrivacidad,
                                    "maxMembers" to nuevaCapacidad,
                                    "lastActivity" to System.currentTimeMillis()
                                )
                            ).addOnSuccessListener {
                                Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                            }
                        }
                        selectedTab = 1
                    },
                    onCerrarSala = {
                        if (groupId.isNotBlank()) {
                            quitarSalaDeLocal()
                            if (esAdminReal) {
                                eliminarSalaCompleta(
                                    groupId = groupId,
                                    codigo = codigoInvitacion,
                                    onComplete = { onBack() }
                                )
                            } else if (currentUserId.isNotBlank()) {
                                salirDeGrupo(
                                    groupId = groupId,
                                    uid = currentUserId,
                                    onSuccess = { onBack() }
                                )
                            }
                        } else {
                            onBack()
                        }
                    },
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