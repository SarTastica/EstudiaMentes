package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SalaRankingTab(
    estudiantes: List<EstudianteEstudio>,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(395.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EstudioColors.white
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 17.dp, bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 17.dp)
                    ) {
                        Text(
                            text = "Compite con tus amigos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EstudioColors.navy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registra tus horas y observa crecer sus árboles",
                            fontSize = 12.sp,
                            color = EstudioColors.secondaryText
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.84f)
                                .align(Alignment.TopCenter)
                                .background(EstudioColors.sky)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.20f)
                                .align(Alignment.BottomCenter)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 44.dp,
                                        topEnd = 44.dp
                                    )
                                )
                                .background(EstudioColors.meadow)
                        )

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = EstudioColors.primary,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                            estudiantes.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aún no hay participantes registrados",
                                        fontSize = 13.sp,
                                        color = EstudioColors.secondaryText,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            else -> {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    items(
                                        items = estudiantes,
                                        key = { it.uid }
                                    ) { estudiante ->
                                        val fase = calcularFaseArbol(estudiante.horas)

                                        Column(
                                            modifier = Modifier
                                                .width(64.dp)
                                                .fillMaxHeight()
                                                .padding(top = 10.dp, bottom = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom
                                        ) {
                                            Text(
                                                text = "${formatearHoras(estudiante.horas)} h",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (estudiante.isCurrentUser) {
                                                    EstudioColors.primary
                                                } else {
                                                    EstudioColors.navy
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                TreeEvolutionIllustration(
                                                    modifier = Modifier
                                                        .width(62.dp)
                                                        .fillMaxHeight(),
                                                    fase = fase,
                                                    canopyColor = estudiante.colorAvatar
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(7.dp))

                                            ProfileAvatar(
                                                avatarResId = estudiante.avatarResId,
                                                nombre = estudiante.nombre,
                                                color = estudiante.colorAvatar
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        color = EstudioColors.primarySoft,
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 10.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🌱", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "¡Tu árbol sigue creciendo!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EstudioColors.navy
                                )
                                Text(
                                    text = "Desliza hacia abajo para ver la clasificación detallada.",
                                    fontSize = 10.sp,
                                    color = EstudioColors.secondaryText
                                )
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(
            items = estudiantes,
            key = { _, item -> "rank_${item.uid}" }
        ) { index, estudiante ->
            RankingBannerItem(
                posicion = index + 1,
                estudiante = estudiante
            )
        }
    }
}

private fun calcularFaseArbol(horas: Float): Int {
    return when {
        horas < 1.5f -> 1
        horas < 2.0f -> 2
        horas < 3.0f -> 3
        horas <= 4.0f -> 4
        else -> 5
    }
}

private fun formatearHoras(horas: Float): String {
    return if (horas % 1f == 0f) {
        horas.toInt().toString()
    } else {
        horas.toString()
    }
}

@Composable
private fun RankingBannerItem(
    posicion: Int,
    estudiante: EstudianteEstudio
) {
    val bannerColor = estudiante.colorAvatar.copy(alpha = 0.24f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = if (posicion == 1) 14.dp else 0.dp),
            shape = RoundedCornerShape(50),
            color = bannerColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 6.dp,
                        end = if (posicion == 1) 34.dp else 24.dp,
                        top = 6.dp,
                        bottom = 6.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = estudiante.colorAvatar
                ) {
                    if (estudiante.avatarResId != null) {
                        Image(
                            painter = painterResource(id = estudiante.avatarResId),
                            contentDescription = "Foto de ${estudiante.nombre}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = estudiante.nombre.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = if (estudiante.isCurrentUser) "${estudiante.nombre} (Tú)" else estudiante.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = EstudioColors.navy,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${formatearHoras(estudiante.horas)} Horas",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EstudioColors.navy
                )
            }
        }

        if (posicion == 1) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.CenterEnd),
                shape = CircleShape,
                color = Color(0xFFFFC107),
                shadowElevation = 3.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Top 1",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    avatarResId: Int?,
    nombre: String,
    color: Color
) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        shadowElevation = 1.dp
    ) {
        if (avatarResId != null) {
            Image(
                painter = painterResource(id = avatarResId),
                contentDescription = "Foto de perfil de $nombre",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = nombre.take(1).uppercase(),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun TreeEvolutionIllustration(
    modifier: Modifier = Modifier,
    fase: Int,
    canopyColor: Color
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val bottomY = size.height * 0.99f

        when (fase) {
            1 -> {
                val stemTop = bottomY - 12.dp.toPx()
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY),
                    end = Offset(centerX, stemTop),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawOval(
                    color = canopyColor,
                    topLeft = Offset(centerX - 11.dp.toPx(), stemTop - 5.dp.toPx()),
                    size = Size(10.dp.toPx(), 6.dp.toPx())
                )
                drawOval(
                    color = canopyColor.copy(alpha = 0.85f),
                    topLeft = Offset(centerX + 1.dp.toPx(), stemTop - 5.dp.toPx()),
                    size = Size(10.dp.toPx(), 6.dp.toPx())
                )
            }
            2 -> {
                val stemTop = bottomY - 40.dp.toPx()
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY),
                    end = Offset(centerX, stemTop),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawOval(
                    color = canopyColor,
                    topLeft = Offset(centerX - 14.dp.toPx(), bottomY - 22.dp.toPx()),
                    size = Size(13.dp.toPx(), 7.dp.toPx())
                )
                drawOval(
                    color = canopyColor.copy(alpha = 0.9f),
                    topLeft = Offset(centerX + 1.dp.toPx(), bottomY - 31.dp.toPx()),
                    size = Size(13.dp.toPx(), 7.dp.toPx())
                )
                drawCircle(
                    color = canopyColor,
                    radius = 5.5.dp.toPx(),
                    center = Offset(centerX, stemTop - 3.dp.toPx())
                )
            }
            3 -> {
                val trunkTop = bottomY - 58.dp.toPx()
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY),
                    end = Offset(centerX, trunkTop),
                    strokeWidth = 5.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                val canopyY = trunkTop - 7.dp.toPx()
                drawCircle(
                    color = canopyColor,
                    radius = 15.dp.toPx(),
                    center = Offset(centerX, canopyY)
                )
                drawCircle(
                    color = canopyColor.copy(alpha = 0.85f),
                    radius = 11.dp.toPx(),
                    center = Offset(centerX - 8.dp.toPx(), canopyY + 4.dp.toPx())
                )
                drawCircle(
                    color = canopyColor.copy(alpha = 0.85f),
                    radius = 11.dp.toPx(),
                    center = Offset(centerX + 8.dp.toPx(), canopyY + 4.dp.toPx())
                )
            }
            4 -> {
                val trunkTop = bottomY - 78.dp.toPx()
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY),
                    end = Offset(centerX, trunkTop),
                    strokeWidth = 7.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY - 48.dp.toPx()),
                    end = Offset(centerX - 14.dp.toPx(), bottomY - 65.dp.toPx()),
                    strokeWidth = 3.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY - 54.dp.toPx()),
                    end = Offset(centerX + 14.dp.toPx(), bottomY - 68.dp.toPx()),
                    strokeWidth = 3.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val canopyY = trunkTop - 12.dp.toPx()
                drawCircle(color = canopyColor.copy(alpha = 0.9f), radius = 20.dp.toPx(), center = Offset(centerX, canopyY - 3.dp.toPx()))
                drawCircle(color = canopyColor, radius = 16.dp.toPx(), center = Offset(centerX - 12.dp.toPx(), canopyY + 6.dp.toPx()))
                drawCircle(color = canopyColor, radius = 16.dp.toPx(), center = Offset(centerX + 12.dp.toPx(), canopyY + 6.dp.toPx()))
            }
            else -> {
                val trunkTop = bottomY - 90.dp.toPx()
                val trunkPath = Path().apply {
                    moveTo(centerX - 5.5.dp.toPx(), bottomY)
                    lineTo(centerX + 5.5.dp.toPx(), bottomY)
                    lineTo(centerX + 3.8.dp.toPx(), trunkTop)
                    lineTo(centerX - 3.8.dp.toPx(), trunkTop)
                    close()
                }
                drawPath(path = trunkPath, color = EstudioColors.trunk)

                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY - 56.dp.toPx()),
                    end = Offset(centerX - 16.dp.toPx(), bottomY - 76.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = EstudioColors.trunk,
                    start = Offset(centerX, bottomY - 62.dp.toPx()),
                    end = Offset(centerX + 16.dp.toPx(), bottomY - 80.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val canopyY = trunkTop - 13.dp.toPx()
                drawCircle(color = canopyColor.copy(alpha = 0.88f), radius = 22.dp.toPx(), center = Offset(centerX, canopyY - 6.dp.toPx()))
                drawCircle(color = canopyColor, radius = 17.dp.toPx(), center = Offset(centerX - 14.dp.toPx(), canopyY + 5.dp.toPx()))
                drawCircle(color = canopyColor, radius = 17.dp.toPx(), center = Offset(centerX + 14.dp.toPx(), canopyY + 5.dp.toPx()))
                drawCircle(color = canopyColor.copy(alpha = 0.95f), radius = 16.dp.toPx(), center = Offset(centerX, canopyY + 10.dp.toPx()))

                val apples = listOf(
                    Offset(centerX - 13.dp.toPx(), canopyY - 2.dp.toPx()),
                    Offset(centerX + 13.dp.toPx(), canopyY + 2.dp.toPx()),
                    Offset(centerX - 2.dp.toPx(), canopyY + 12.dp.toPx()),
                    Offset(centerX + 5.dp.toPx(), canopyY - 12.dp.toPx()),
                    Offset(centerX - 15.dp.toPx(), canopyY + 9.dp.toPx())
                )
                apples.forEach { pos ->
                    drawCircle(color = EstudioColors.apple, radius = 3.8.dp.toPx(), center = pos)
                    drawCircle(
                        color = EstudioColors.appleHighlight,
                        radius = 1.3.dp.toPx(),
                        center = Offset(pos.x - 1.dp.toPx(), pos.y - 1.dp.toPx())
                    )
                }
            }
        }
    }
}