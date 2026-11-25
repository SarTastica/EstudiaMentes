package com.example.estudiamentes.ui.estudio

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.data.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.Locale

private val BluePrimary = Color(0xFF5A9BFF)
private val BlueLight = Color(0xFFE8F1FF)
private val TextGray = Color(0xFF6E829E)
private val TextDark = Color(0xFF1A1F36)
private val TreeGreen = Color(0xFF4CAF50)

@Composable
fun EstudioScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val profileRepository = remember { ProfileRepository() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val prefs = remember {
        context.getSharedPreferences("estudiamentes_pomodoro_presets", Context.MODE_PRIVATE)
    }

    var manzanas by remember { mutableIntStateOf(15) }
    var rachaDias by remember { mutableIntStateOf(0) }
    var itemsDesbloqueados by remember { mutableStateOf(listOf<String>()) }
    var itemsEquipados by remember { mutableStateOf(listOf<String>()) }
    var mostrarTienda by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        profileRepository.cargarEstadoHuerto(context) { m, r, desb, equip ->
            manzanas = m
            rachaDias = r
            itemsDesbloqueados = desb
            itemsEquipados = equip
        }
    }

    var presets by remember { mutableStateOf(cargarPresetsGuardados(context)) }
    var presetSeleccionadoId by remember {
        mutableStateOf(prefs.getString("selected_preset_id", "clasico") ?: "clasico")
    }

    val presetActual = presets.find { it.id == presetSeleccionadoId } ?: presets[1]

    var faseActual by remember { mutableStateOf("ESTUDIO") }
    var mostrarAjustes by remember { mutableStateOf(false) }

    var tiempoTotalSegundos by remember { mutableIntStateOf(presetActual.estudioMin * 60) }
    var segundosRestantes by remember { mutableIntStateOf(presetActual.estudioMin * 60) }
    var estaActivo by remember { mutableStateOf(false) }

    fun actualizarPresetActual(nuevoEstudio: Int, nuevoDescanso: Int) {
        val actualizado = presetActual.copy(
            estudioMin = nuevoEstudio.coerceIn(1, 240),
            descansoMin = nuevoDescanso.coerceIn(1, 60)
        )
        presets = presets.map { if (it.id == actualizado.id) actualizado else it }
        guardarPresetEnPreferencias(context, actualizado)
        tiempoTotalSegundos = actualizado.estudioMin * 60
        segundosRestantes = actualizado.estudioMin * 60
    }

    LaunchedEffect(estaActivo, segundosRestantes, faseActual) {
        if (estaActivo && segundosRestantes > 0) {
            delay(1000L)
            segundosRestantes--
        } else if (segundosRestantes == 0 && estaActivo) {
            if (faseActual == "ESTUDIO") {
                val minutosCompletados = (tiempoTotalSegundos / 60).coerceAtLeast(1)
                guardarHorasEstudioEnSalas(db, currentUserId, tiempoTotalSegundos, context)
                profileRepository.registrarCosechaEstudio(context, minutosCompletados) { nuevasM, nuevaR, ganadas ->
                    manzanas = nuevasM
                    rachaDias = nuevaR
                    Toast.makeText(
                        context,
                        "¡Cosecha lista! +$ganadas 🍎 (Racha: $nuevaR días)",
                        Toast.LENGTH_LONG
                    ).show()
                }
                faseActual = "DESCANSO"
                tiempoTotalSegundos = presetActual.descansoMin * 60
                segundosRestantes = presetActual.descansoMin * 60
            } else {
                estaActivo = false
                faseActual = "ESTUDIO"
                tiempoTotalSegundos = presetActual.estudioMin * 60
                segundosRestantes = presetActual.estudioMin * 60
            }
        }
    }

    val horas = segundosRestantes / 3600
    val minutos = (segundosRestantes % 3600) / 60
    val segundos = segundosRestantes % 60
    val formatoHora = String.format(Locale.getDefault(), "%02d:%02d:%02d", horas, minutos, segundos)

    val progresoObjetivo = if (tiempoTotalSegundos > 0) {
        (1f - (segundosRestantes.toFloat() / tiempoTotalSegundos.toFloat())).coerceIn(0f, 1f)
    } else {
        0f
    }

    val progresoAnimado by animateFloatAsState(
        targetValue = if (faseActual == "ESTUDIO") progresoObjetivo else 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progresoArbolPrado"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        PradoConArbolCanvas(
            progreso = progresoAnimado,
            esDescanso = faseActual == "DESCANSO",
            itemsEquipados = itemsEquipados,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.93f),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🍎", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$manzanas",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.93f),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Racha",
                                    tint = Color(0xFFFF7043),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (rachaDias >= 3) "${rachaDias}d · x1.5" else "${rachaDias}d",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = { mostrarTienda = true },
                        shape = RoundedCornerShape(50),
                        color = BluePrimary,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Personalizar prado",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mi Prado",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White.copy(alpha = 0.93f),
                    shadowElevation = 6.dp,
                    border = BorderStroke(
                        1.5.dp,
                        if (faseActual == "ESTUDIO") BluePrimary.copy(alpha = 0.25f)
                        else TreeGreen.copy(alpha = 0.30f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (faseActual == "ESTUDIO") "ESTUDIO · ${presetActual.nombre.uppercase()}" else "DESCANSO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp,
                                color = if (faseActual == "ESTUDIO") BluePrimary else TreeGreen
                            )

                            if (!estaActivo && faseActual == "ESTUDIO") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    onClick = { mostrarAjustes = !mostrarAjustes },
                                    shape = CircleShape,
                                    color = if (mostrarAjustes) BluePrimary else BlueLight,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Ajustar tiempos",
                                            tint = if (mostrarAjustes) Color.White else BluePrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = formatoHora,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = TextDark,
                            letterSpacing = 2.sp
                        )

                        AnimatedVisibility(visible = mostrarAjustes && !estaActivo && faseActual == "ESTUDIO") {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    color = BluePrimary.copy(alpha = 0.15f)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    presets.forEach { preset ->
                                        val esSeleccionado = preset.id == presetSeleccionadoId
                                        Surface(
                                            onClick = {
                                                presetSeleccionadoId = preset.id
                                                prefs.edit().putString("selected_preset_id", preset.id).apply()
                                                tiempoTotalSegundos = preset.estudioMin * 60
                                                segundosRestantes = preset.estudioMin * 60
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (esSeleccionado) BluePrimary else BlueLight,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = preset.nombre,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (esSeleccionado) Color.White else TextDark
                                                )
                                                Text(
                                                    text = "${preset.estudioMin}/${preset.descansoMin}m",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (esSeleccionado) Color.White.copy(alpha = 0.9f) else BluePrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SelectorMinutosCompacto("Estudio", presetActual.estudioMin) { nuevoEstudio ->
                                        actualizarPresetActual(nuevoEstudio, presetActual.descansoMin)
                                    }
                                    SelectorMinutosCompacto("Descanso", presetActual.descansoMin) { nuevoDescanso ->
                                        actualizarPresetActual(presetActual.estudioMin, nuevoDescanso)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = {
                        if (faseActual == "ESTUDIO") {
                            val segundosEstudiados = tiempoTotalSegundos - segundosRestantes
                            if (segundosEstudiados >= 60) {
                                guardarHorasEstudioEnSalas(db, currentUserId, segundosEstudiados, context)
                            }
                            if (segundosEstudiados > 0) {
                                Toast.makeText(
                                    context,
                                    "Sesión cancelada: completa el bloque para cosechar 🍎",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        estaActivo = false
                        segundosRestantes = tiempoTotalSegundos
                    },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reiniciar",
                            tint = TextGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Button(
                    onClick = {
                        mostrarAjustes = false
                        estaActivo = !estaActivo
                    },
                    modifier = Modifier
                        .height(56.dp)
                        .width(160.dp),
                    shape = RoundedCornerShape(50),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (faseActual == "ESTUDIO") BluePrimary else TreeGreen
                    )
                ) {
                    Icon(
                        imageVector = if (estaActivo) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (estaActivo) "Pausar" else "Empezar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (faseActual == "DESCANSO") {
                    Spacer(modifier = Modifier.width(18.dp))
                    Surface(
                        onClick = {
                            estaActivo = false
                            faseActual = "ESTUDIO"
                            tiempoTotalSegundos = presetActual.estudioMin * 60
                            segundosRestantes = presetActual.estudioMin * 60
                        },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Salir del descanso",
                                tint = TreeGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }

        if (mostrarTienda) {
            TiendaPradoSheet(
                manzanas = manzanas,
                itemsDesbloqueados = itemsDesbloqueados,
                itemsEquipados = itemsEquipados,
                profileRepository = profileRepository,
                onActualizarEstado = { nuevoSaldo, nuevosDesb, nuevosEquip ->
                    manzanas = nuevoSaldo
                    itemsDesbloqueados = nuevosDesb
                    itemsEquipados = nuevosEquip
                },
                onDismiss = { mostrarTienda = false }
            )
        }
    }
}

@Composable
private fun SelectorMinutosCompacto(label: String, valorActual: Int, onCambiar: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(26.dp)
                    .clickable { if (valorActual > 1) onCambiar(valorActual - 1) },
                shape = CircleShape,
                color = BlueLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("-", fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }
            Text(
                text = "${valorActual}m",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Surface(
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onCambiar(valorActual + 1) },
                shape = CircleShape,
                color = BlueLight
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }
        }
    }
}

fun guardarHorasEstudioEnSalas(
    db: FirebaseFirestore,
    userId: String,
    segundosEstudiados: Int,
    context: Context
) {
    if (userId.isBlank() || segundosEstudiados <= 0) return

    val horasAñadir = segundosEstudiados / 3600f

    db.collection("groups")
        .whereArrayContains("memberIds", userId)
        .get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) return@addOnSuccessListener

            val batch = db.batch()
            for (doc in snapshot.documents) {
                val miembroRef = doc.reference.collection("members").document(userId)
                batch.update(miembroRef, "studyHours", FieldValue.increment(horasAñadir.toDouble()))
            }

            batch.commit().addOnSuccessListener {
                Toast.makeText(
                    context,
                    String.format(Locale.getDefault(), "+%.1fh sumadas a tus salas", horasAñadir),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}