package com.example.estudiamentes.ui.estudio

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private val TreeGreenDark = Color(0xFF2E7D32)
private val TreeGreenLight = Color(0xFF81C784)
private val TrunkBrown = Color(0xFF795548)
private val SoilBrown = Color(0xFF5D4037)
private val AppleRed = Color(0xFFFF5252)

private data class PresetCiclo(
    val id: String,
    val nombre: String,
    val estudioMin: Int,
    val descansoMin: Int
)

private fun cargarPresetsGuardados(context: Context): List<PresetCiclo> {
    val prefs = context.getSharedPreferences("estudiamentes_pomodoro_presets", Context.MODE_PRIVATE)
    return listOf(
        PresetCiclo(
            id = "leve",
            nombre = "Leve",
            estudioMin = prefs.getInt("leve_estudio", 10),
            descansoMin = prefs.getInt("leve_descanso", 5)
        ),
        PresetCiclo(
            id = "clasico",
            nombre = "Clásico",
            estudioMin = prefs.getInt("clasico_estudio", 25),
            descansoMin = prefs.getInt("clasico_descanso", 5)
        ),
        PresetCiclo(
            id = "intenso",
            nombre = "Intenso",
            estudioMin = prefs.getInt("intenso_estudio", 50),
            descansoMin = prefs.getInt("intenso_descanso", 10)
        )
    )
}

private fun guardarPresetEnPreferencias(context: Context, preset: PresetCiclo) {
    val prefs = context.getSharedPreferences("estudiamentes_pomodoro_presets", Context.MODE_PRIVATE)
    prefs.edit()
        .putInt("${preset.id}_estudio", preset.estudioMin)
        .putInt("${preset.id}_descanso", preset.descansoMin)
        .apply()
}

@Composable
fun EstudioScreen(
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val prefs = remember {
        context.getSharedPreferences("estudiamentes_pomodoro_presets", Context.MODE_PRIVATE)
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
                guardarHorasEstudioEnSalas(db, currentUserId, tiempoTotalSegundos, context)
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
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
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

                            // Selección de perfil (cada uno guarda sus propios minutos)
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

                            // Modificar los tiempos del perfil seleccionado y guardarlos automáticamente
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

            // Controles inferiores sobre el prado
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

@Composable
private fun PradoConArbolCanvas(
    progreso: Float,
    esDescanso: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val horizonY = h * 0.64f
        val groundY = h * 0.72f

        val skyColors = if (esDescanso) {
            listOf(Color(0xFFD8F3DC), Color(0xFFF1FAEE))
        } else {
            listOf(Color(0xFFD6E8FF), Color(0xFFF2F7FF))
        }
        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = horizonY
            ),
            size = Size(w, horizonY + 40.dp.toPx())
        )

        drawCircle(
            color = Color(0xFFFFE082).copy(alpha = 0.55f),
            radius = 46.dp.toPx(),
            center = Offset(w * 0.84f, h * 0.24f)
        )
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.85f),
            radius = 28.dp.toPx(),
            center = Offset(w * 0.84f, h * 0.24f)
        )

        drawCloud(center = Offset(w * 0.20f, h * 0.26f), scale = 1.0f)
        drawCloud(center = Offset(w * 0.72f, h * 0.34f), scale = 0.8f)

        drawOval(
            color = Color(0xFFA5D6A7),
            topLeft = Offset(-w * 0.25f, horizonY - 24.dp.toPx()),
            size = Size(w * 0.95f, h * 0.45f)
        )
        drawOval(
            color = Color(0xFF9CCC65).copy(alpha = 0.65f),
            topLeft = Offset(w * 0.25f, horizonY - 14.dp.toPx()),
            size = Size(w * 1.05f, h * 0.45f)
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF81C784), Color(0xFF66BB6A)),
                startY = horizonY + 12.dp.toPx(),
                endY = h
            ),
            topLeft = Offset(0f, horizonY + 12.dp.toPx()),
            size = Size(w, h - horizonY)
        )

        val grassSpots = listOf(
            Offset(w * 0.16f, groundY + 18.dp.toPx()),
            Offset(w * 0.82f, groundY + 12.dp.toPx()),
            Offset(w * 0.25f, groundY + 46.dp.toPx()),
            Offset(w * 0.75f, groundY + 42.dp.toPx())
        )
        grassSpots.forEach { spot ->
            drawLine(
                color = TreeGreenDark.copy(alpha = 0.45f),
                start = spot,
                end = Offset(spot.x - 4.dp.toPx(), spot.y - 10.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = TreeGreenDark.copy(alpha = 0.45f),
                start = spot,
                end = Offset(spot.x + 4.dp.toPx(), spot.y - 9.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        drawOval(
            color = TreeGreenDark.copy(alpha = 0.22f),
            topLeft = Offset(centerX - (42.dp.toPx() + 55.dp.toPx() * progreso), groundY - 6.dp.toPx()),
            size = Size((84.dp.toPx() + 110.dp.toPx() * progreso), 22.dp.toPx())
        )

        drawOval(
            color = SoilBrown,
            topLeft = Offset(centerX - 38.dp.toPx(), groundY - 8.dp.toPx()),
            size = Size(76.dp.toPx(), 18.dp.toPx())
        )

        if (progreso < 0.10f) {
            val semillaScale = 0.75f + (progreso / 0.10f) * 0.25f
            drawOval(
                color = Color(0xFF8D6E63),
                topLeft = Offset(
                    centerX - 10.dp.toPx() * semillaScale,
                    groundY - 10.dp.toPx() * semillaScale
                ),
                size = Size(20.dp.toPx() * semillaScale, 13.dp.toPx() * semillaScale)
            )
            return@Canvas
        }

        val p = ((progreso - 0.10f) / 0.90f).coerceIn(0f, 1f)

        val maxTrunkHeight = h * 0.30f
        val trunkHeight = (h * 0.05f) + (maxTrunkHeight - h * 0.05f) * p
        val trunkWidth = (6.dp.toPx()) + (22.dp.toPx()) * p
        val trunkTopY = groundY - trunkHeight

        drawRoundRect(
            color = TrunkBrown,
            topLeft = Offset(centerX - trunkWidth / 2f, trunkTopY),
            size = Size(trunkWidth, trunkHeight + 4.dp.toPx()),
            cornerRadius = CornerRadius(trunkWidth / 2f, trunkWidth / 2f)
        )

        if (p < 0.30f) {
            val leafFactor = (p / 0.30f).coerceIn(0.25f, 1f)
            val leftLeaf = Path().apply {
                moveTo(centerX, trunkTopY + 4.dp.toPx())
                quadraticTo(
                    centerX - 30.dp.toPx() * leafFactor,
                    trunkTopY - 20.dp.toPx() * leafFactor,
                    centerX - 42.dp.toPx() * leafFactor,
                    trunkTopY + 2.dp.toPx()
                )
                quadraticTo(
                    centerX - 18.dp.toPx() * leafFactor,
                    trunkTopY + 14.dp.toPx() * leafFactor,
                    centerX,
                    trunkTopY + 4.dp.toPx()
                )
            }
            val rightLeaf = Path().apply {
                moveTo(centerX, trunkTopY + 4.dp.toPx())
                quadraticTo(
                    centerX + 30.dp.toPx() * leafFactor,
                    trunkTopY - 20.dp.toPx() * leafFactor,
                    centerX + 42.dp.toPx() * leafFactor,
                    trunkTopY + 2.dp.toPx()
                )
                quadraticTo(
                    centerX + 18.dp.toPx() * leafFactor,
                    trunkTopY + 14.dp.toPx() * leafFactor,
                    centerX,
                    trunkTopY + 4.dp.toPx()
                )
            }
            drawPath(leftLeaf, TreeGreen)
            drawPath(rightLeaf, TreeGreenLight)
            return@Canvas
        }

        if (p > 0.45f) {
            val branchFactor = ((p - 0.45f) / 0.55f).coerceIn(0f, 1f)
            val branchStartY = trunkTopY + trunkHeight * 0.45f
            drawLine(
                color = TrunkBrown,
                start = Offset(centerX, branchStartY),
                end = Offset(
                    centerX - 34.dp.toPx() * branchFactor,
                    branchStartY - 24.dp.toPx() * branchFactor
                ),
                strokeWidth = trunkWidth * 0.45f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = TrunkBrown,
                start = Offset(centerX, branchStartY - 6.dp.toPx()),
                end = Offset(
                    centerX + 34.dp.toPx() * branchFactor,
                    branchStartY - 28.dp.toPx() * branchFactor
                ),
                strokeWidth = trunkWidth * 0.45f,
                cap = StrokeCap.Round
            )
        }

        val crownScale = ((p - 0.25f) / 0.75f).coerceIn(0.18f, 1f)
        val baseRadius = w * 0.22f * crownScale

        drawCircle(
            color = TreeGreenDark,
            radius = baseRadius * 0.98f,
            center = Offset(centerX, trunkTopY - baseRadius * 0.35f)
        )
        drawCircle(
            color = TreeGreen,
            radius = baseRadius * 0.88f,
            center = Offset(centerX - baseRadius * 0.85f, trunkTopY + baseRadius * 0.15f)
        )
        drawCircle(
            color = TreeGreen,
            radius = baseRadius * 0.88f,
            center = Offset(centerX + baseRadius * 0.85f, trunkTopY + baseRadius * 0.15f)
        )
        drawCircle(
            color = TreeGreenLight,
            radius = baseRadius,
            center = Offset(centerX, trunkTopY - baseRadius * 0.15f)
        )

        if (progreso >= 0.85f) {
            val appleScale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
            val appleRadius = 7.dp.toPx() * appleScale
            val applePositions = listOf(
                Offset(centerX - baseRadius * 0.65f, trunkTopY + baseRadius * 0.10f),
                Offset(centerX + baseRadius * 0.55f, trunkTopY - baseRadius * 0.20f),
                Offset(centerX - baseRadius * 0.10f, trunkTopY - baseRadius * 0.55f),
                Offset(centerX + baseRadius * 0.70f, trunkTopY + baseRadius * 0.35f),
                Offset(centerX - baseRadius * 0.45f, trunkTopY - baseRadius * 0.32f)
            )
            applePositions.forEach { pos ->
                drawCircle(color = AppleRed, radius = appleRadius, center = pos)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    center: Offset,
    scale: Float
) {
    val cloudColor = Color.White.copy(alpha = 0.85f)
    val r = 18.dp.toPx() * scale
    drawCircle(color = cloudColor, radius = r, center = center)
    drawCircle(color = cloudColor, radius = r * 0.8f, center = Offset(center.x - r * 0.95f, center.y + r * 0.15f))
    drawCircle(color = cloudColor, radius = r * 0.85f, center = Offset(center.x + r * 0.95f, center.y + r * 0.12f))
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