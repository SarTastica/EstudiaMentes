package com.example.estudiamentes.ui.estudio

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val FlatGreenLight = Color(0xFF9CCC65)
private val FlatGreenDark = Color(0xFF8BC34A)
private val FlatGreenDot = Color(0xFF689F38)
private val FlatTrunkLight = Color(0xFF6D4C41)
private val FlatTrunkDark = Color(0xFF4E342E)
private val SoilBrown = Color(0xFF5D4037)
private val AppleRed = Color(0xFFFF5252)

@Composable
fun PradoConArbolCanvas(
    progreso: Float,
    esDescanso: Boolean,
    itemsEquipados: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val horizonY = h * 0.65f
        val groundY = h * 0.74f

        val esNoche = itemsEquipados.contains("paleta_noche")
        val esAtardecer = itemsEquipados.contains("paleta_atardecer")
        val esBosque = itemsEquipados.contains("paleta_bosque")
        val esPastel = itemsEquipados.contains("paleta_pastel")

        val tipoArbol = when {
            itemsEquipados.contains("arbol_cipres") -> "CIPRES"
            itemsEquipados.contains("arbol_pino") -> "PINO"
            itemsEquipados.contains("arbol_bonsai") -> "BONSAI"
            itemsEquipados.contains("arbol_palmera") -> "PALMERA"
            itemsEquipados.contains("arbol_sakura") -> "SAKURA"
            else -> "DEFAULT"
        }

        val skyColors = when {
            esNoche -> listOf(Color(0xFF0F172A), Color(0xFF1E293B))
            esAtardecer -> listOf(Color(0xFFFF8A65), Color(0xFFFFE082))
            esBosque -> listOf(Color(0xFFB2DFDB), Color(0xFFE0F2F1))
            esPastel -> listOf(Color(0xFFF3E5F5), Color(0xFFE1F5FE))
            esDescanso -> listOf(Color(0xFFD8F3DC), Color(0xFFF1FAEE))
            else -> listOf(Color(0xFFD6E8FF), Color(0xFFF2F7FF))
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = horizonY
            ),
            size = Size(w, horizonY + 40.dp.toPx())
        )

        if (esNoche) {
            val estrellas = listOf(
                Offset(w * 0.15f, h * 0.18f),
                Offset(w * 0.32f, h * 0.12f),
                Offset(w * 0.55f, h * 0.15f),
                Offset(w * 0.75f, h * 0.11f),
                Offset(w * 0.88f, h * 0.28f),
                Offset(w * 0.22f, h * 0.35f),
                Offset(w * 0.65f, h * 0.30f)
            )
            estrellas.forEach { star ->
                drawCircle(
                    color = Color(0xFFFFF59D),
                    radius = 2.5.dp.toPx(),
                    center = star
                )
            }
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 22.dp.toPx(),
                center = Offset(w * 0.84f, h * 0.22f)
            )
        } else {
            drawCircle(
                color = if (esAtardecer) Color(0xFFFF7043).copy(alpha = 0.5f) else Color(0xFFFFE082).copy(alpha = 0.55f),
                radius = 46.dp.toPx(),
                center = Offset(w * 0.84f, h * 0.24f)
            )
            drawCircle(
                color = if (esAtardecer) Color(0xFFFFAB40) else Color(0xFFFFD54F).copy(alpha = 0.85f),
                radius = 28.dp.toPx(),
                center = Offset(w * 0.84f, h * 0.24f)
            )
            drawCloud(center = Offset(w * 0.20f, h * 0.26f), scale = 1.0f)
            drawCloud(center = Offset(w * 0.72f, h * 0.34f), scale = 0.8f)
        }

        val colina1 = when {
            esNoche -> Color(0xFF2E7D32)
            esBosque -> Color(0xFF80CBC4)
            esPastel -> Color(0xFFC5E1A5)
            else -> Color(0xFFA5D6A7)
        }
        val colina2 = when {
            esNoche -> Color(0xFF1B5E20).copy(alpha = 0.8f)
            esBosque -> Color(0xFF4DB6AC).copy(alpha = 0.7f)
            else -> Color(0xFF9CCC65).copy(alpha = 0.65f)
        }

        drawOval(
            color = colina1,
            topLeft = Offset(-w * 0.25f, horizonY - 24.dp.toPx()),
            size = Size(w * 0.95f, h * 0.45f)
        )
        drawOval(
            color = colina2,
            topLeft = Offset(w * 0.25f, horizonY - 14.dp.toPx()),
            size = Size(w * 1.05f, h * 0.45f)
        )

        val pradoColors = when {
            esNoche -> listOf(Color(0xFF2E7D32), Color(0xFF1B5E20))
            esBosque -> listOf(Color(0xFF66BB6A), Color(0xFF388E3C))
            esPastel -> listOf(Color(0xFFAED581), Color(0xFF81C784))
            else -> listOf(Color(0xFF81C784), Color(0xFF66BB6A))
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = pradoColors,
                startY = horizonY + 12.dp.toPx(),
                endY = h
            ),
            topLeft = Offset(0f, horizonY + 12.dp.toPx()),
            size = Size(w, h - horizonY)
        )

        if (itemsEquipados.contains("decor_valla")) {
            val fenceY = horizonY + 16.dp.toPx()
            drawLine(
                color = Color(0xFF8D6E63),
                start = Offset(w * 0.06f, fenceY),
                end = Offset(w * 0.94f, fenceY),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color(0xFF8D6E63),
                start = Offset(w * 0.06f, fenceY + 10.dp.toPx()),
                end = Offset(w * 0.94f, fenceY + 10.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
            for (i in 1..8) {
                val fx = w * (i * 0.11f)
                drawRoundRect(
                    color = Color(0xFFA1887F),
                    topLeft = Offset(fx - 3.dp.toPx(), fenceY - 8.dp.toPx()),
                    size = Size(6.dp.toPx(), 24.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }

        if (itemsEquipados.contains("flores_margaritas")) {
            val margaritas = listOf(
                Offset(w * 0.16f, groundY + 14.dp.toPx()),
                Offset(w * 0.24f, groundY + 34.dp.toPx()),
                Offset(w * 0.78f, groundY + 18.dp.toPx()),
                Offset(w * 0.84f, groundY + 40.dp.toPx())
            )
            margaritas.forEach { pos ->
                drawFlower(pos, Color.White, Color(0xFFFFD54F))
            }
        }

        if (itemsEquipados.contains("flores_tulipanes")) {
            val tulipanes = listOf(
                Offset(w * 0.14f, groundY + 46.dp.toPx()),
                Offset(w * 0.30f, groundY + 20.dp.toPx()),
                Offset(w * 0.72f, groundY + 30.dp.toPx()),
                Offset(w * 0.86f, groundY + 14.dp.toPx())
            )
            tulipanes.forEach { pos ->
                drawTulip(pos, Color(0xFFF06292))
            }
        }

        if (itemsEquipados.contains("flores_lavanda")) {
            val lavandas = listOf(
                Offset(w * 0.20f, groundY + 54.dp.toPx()),
                Offset(w * 0.76f, groundY + 54.dp.toPx()),
                Offset(w * 0.34f, groundY + 44.dp.toPx()),
                Offset(w * 0.66f, groundY + 44.dp.toPx())
            )
            lavandas.forEach { pos ->
                drawLavender(pos)
            }
        }

        if (itemsEquipados.contains("flores_girasoles")) {
            val girasoles = listOf(
                Offset(w * 0.12f, groundY + 24.dp.toPx()),
                Offset(w * 0.88f, groundY + 26.dp.toPx())
            )
            girasoles.forEach { pos ->
                drawFlower(pos, Color(0xFFFFCA28), Color(0xFF5D4037))
            }
        }

        if (itemsEquipados.contains("decor_setas")) {
            drawMushroom(Offset(centerX - 66.dp.toPx(), groundY + 12.dp.toPx()), scale = 1.0f)
            drawMushroom(Offset(centerX - 82.dp.toPx(), groundY + 20.dp.toPx()), scale = 0.75f)
        }

        if (itemsEquipados.contains("decor_libros")) {
            val bx = centerX + 54.dp.toPx()
            val by = groundY + 6.dp.toPx()
            drawRoundRect(
                color = Color(0xFF1E88E5),
                topLeft = Offset(bx, by),
                size = Size(28.dp.toPx(), 7.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFE53935),
                topLeft = Offset(bx + 2.dp.toPx(), by - 6.dp.toPx()),
                size = Size(24.dp.toPx(), 6.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFFDD835),
                topLeft = Offset(bx + 4.dp.toPx(), by - 11.dp.toPx()),
                size = Size(20.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }

        if (itemsEquipados.contains("decor_banco")) {
            val benchX = centerX - 135.dp.toPx()
            val benchY = groundY - 4.dp.toPx()
            drawRoundRect(
                color = Color(0xFF6D4C41),
                topLeft = Offset(benchX, benchY - 12.dp.toPx()),
                size = Size(44.dp.toPx(), 6.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(benchX, benchY),
                size = Size(44.dp.toPx(), 6.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            drawLine(
                color = Color(0xFF4E342E),
                start = Offset(benchX + 6.dp.toPx(), benchY - 12.dp.toPx()),
                end = Offset(benchX + 6.dp.toPx(), benchY + 14.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = Color(0xFF4E342E),
                start = Offset(benchX + 38.dp.toPx(), benchY - 12.dp.toPx()),
                end = Offset(benchX + 38.dp.toPx(), benchY + 14.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
        }

        if (itemsEquipados.contains("decor_farol")) {
            val lx = centerX + 94.dp.toPx()
            val ly = groundY - 26.dp.toPx()
            drawLine(
                color = Color(0xFF37474F),
                start = Offset(lx, groundY + 8.dp.toPx()),
                end = Offset(lx, ly),
                strokeWidth = 3.dp.toPx()
            )
            drawCircle(
                color = Color(0xFFFFEE58).copy(alpha = 0.45f),
                radius = 16.dp.toPx(),
                center = Offset(lx, ly)
            )
            drawRoundRect(
                color = Color(0xFFFFCA28),
                topLeft = Offset(lx - 5.dp.toPx(), ly - 7.dp.toPx()),
                size = Size(10.dp.toPx(), 14.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }

        drawOval(
            color = Color(0xFF2E7D32).copy(alpha = 0.22f),
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

        val maxTrunkHeight = when (tipoArbol) {
            "CIPRES" -> h * 0.15f
            "PALMERA" -> h * 0.23f
            "PINO" -> h * 0.16f
            else -> h * 0.18f
        }
        val trunkHeight = (h * 0.04f) + (maxTrunkHeight - h * 0.04f) * p
        val trunkTopY = groundY - trunkHeight

        if (p < 0.28f) {
            drawFlatTaperedTrunk(centerX, groundY, trunkTopY, 5.dp.toPx(), 12.dp.toPx())
            val leafFactor = (p / 0.28f).coerceIn(0.25f, 1f)
            val c1 = if (tipoArbol == "SAKURA") Color(0xFFF48FB1) else FlatGreenLight
            val c2 = if (tipoArbol == "SAKURA") Color(0xFFEC407A) else FlatGreenDark

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
            drawPath(leftLeaf, c1)
            drawPath(rightLeaf, c2)
            return@Canvas
        }

        val crownScale = ((p - 0.22f) / 0.78f).coerceIn(0.20f, 1f)

        when (tipoArbol) {
            "CIPRES" -> drawFlatCypress(centerX, groundY, trunkTopY, w, h, crownScale, progreso)
            "PINO" -> drawFlatPine(centerX, groundY, trunkTopY, trunkHeight, w, crownScale, progreso)
            "BONSAI" -> drawFlatBonsai(centerX, groundY, trunkTopY, trunkHeight, w, crownScale, progreso)
            "PALMERA" -> drawFlatPalm(centerX, groundY, trunkTopY, trunkHeight, w, crownScale, progreso)
            "SAKURA" -> drawFlatFlowerTree(centerX, groundY, trunkTopY, w, crownScale, progreso)
            else -> drawFlatRoundAppleTree(centerX, groundY, trunkTopY, w, crownScale, progreso)
        }
    }
}

private fun DrawScope.drawFlatTaperedTrunk(
    centerX: Float,
    groundY: Float,
    topY: Float,
    topHalfWidth: Float,
    baseHalfWidth: Float
) {
    val leftPath = Path().apply {
        moveTo(centerX - topHalfWidth, topY)
        lineTo(centerX, topY)
        lineTo(centerX, groundY + 2.dp.toPx())
        lineTo(centerX - baseHalfWidth, groundY + 2.dp.toPx())
        quadraticTo(
            centerX - baseHalfWidth * 0.55f,
            (topY + groundY) / 2f,
            centerX - topHalfWidth,
            topY
        )
        close()
    }
    val rightPath = Path().apply {
        moveTo(centerX, topY)
        lineTo(centerX + topHalfWidth, topY)
        quadraticTo(
            centerX + baseHalfWidth * 0.55f,
            (topY + groundY) / 2f,
            centerX + baseHalfWidth,
            groundY + 2.dp.toPx()
        )
        lineTo(centerX, groundY + 2.dp.toPx())
        close()
    }
    drawPath(leftPath, FlatTrunkLight)
    drawPath(rightPath, FlatTrunkDark)
}

private fun DrawScope.drawFlatRoundAppleTree(
    centerX: Float,
    groundY: Float,
    trunkTopY: Float,
    w: Float,
    crownScale: Float,
    progreso: Float
) {
    drawFlatTaperedTrunk(
        centerX = centerX,
        groundY = groundY,
        topY = trunkTopY - 10.dp.toPx() * crownScale,
        topHalfWidth = 9.dp.toPx() * crownScale,
        baseHalfWidth = 24.dp.toPx() * crownScale
    )

    val r = w * 0.24f * crownScale
    val cy = trunkTopY - r * 0.72f

    drawCircle(color = FlatGreenLight, radius = r, center = Offset(centerX, cy))

    clipRect(left = centerX, top = cy - r - 2f, right = centerX + r + 2f, bottom = cy + r + 2f) {
        drawCircle(color = FlatGreenDark, radius = r, center = Offset(centerX, cy))
    }

    val dots = listOf(
        Offset(centerX - r * 0.45f, cy - r * 0.35f),
        Offset(centerX - r * 0.55f, cy + r * 0.15f),
        Offset(centerX - r * 0.22f, cy + r * 0.45f),
        Offset(centerX - r * 0.18f, cy - r * 0.55f),
        Offset(centerX + r * 0.35f, cy - r * 0.40f),
        Offset(centerX + r * 0.52f, cy + r * 0.05f),
        Offset(centerX + r * 0.28f, cy + r * 0.48f),
        Offset(centerX + r * 0.15f, cy - r * 0.08f)
    )
    dots.forEach { d ->
        drawCircle(color = FlatGreenDot, radius = 3.6.dp.toPx() * crownScale, center = d)
    }

    if (progreso >= 0.85f) {
        val appleScale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val appleR = 6.5.dp.toPx() * appleScale
        val apples = listOf(
            Offset(centerX - r * 0.48f, cy + r * 0.05f),
            Offset(centerX + r * 0.42f, cy - r * 0.22f),
            Offset(centerX - r * 0.12f, cy - r * 0.45f),
            Offset(centerX + r * 0.36f, cy + r * 0.36f),
            Offset(centerX - r * 0.25f, cy + r * 0.48f)
        )
        apples.forEach { pos ->
            drawCircle(color = AppleRed, radius = appleR, center = pos)
        }
    }
}

private fun DrawScope.drawFlatCypress(
    centerX: Float,
    groundY: Float,
    trunkTopY: Float,
    w: Float,
    h: Float,
    crownScale: Float,
    progreso: Float
) {
    drawFlatTaperedTrunk(
        centerX = centerX,
        groundY = groundY,
        topY = trunkTopY - 8.dp.toPx() * crownScale,
        topHalfWidth = 7.dp.toPx() * crownScale,
        baseHalfWidth = 20.dp.toPx() * crownScale
    )

    val ovalW = w * 0.24f * crownScale
    val ovalH = h * 0.24f * crownScale
    val topY = trunkTopY - ovalH * 0.88f

    drawOval(
        color = FlatGreenLight,
        topLeft = Offset(centerX - ovalW / 2f, topY),
        size = Size(ovalW, ovalH)
    )
    clipRect(left = centerX, top = topY - 2f, right = centerX + ovalW, bottom = topY + ovalH + 2f) {
        drawOval(
            color = FlatGreenDark,
            topLeft = Offset(centerX - ovalW / 2f, topY),
            size = Size(ovalW, ovalH)
        )
    }

    val streaks = listOf(
        Offset(centerX - ovalW * 0.18f, topY + ovalH * 0.25f),
        Offset(centerX - ovalW * 0.22f, topY + ovalH * 0.58f),
        Offset(centerX + ovalW * 0.16f, topY + ovalH * 0.38f),
        Offset(centerX + ovalW * 0.20f, topY + ovalH * 0.72f)
    )
    streaks.forEach { s ->
        drawOval(
            color = FlatGreenDot,
            topLeft = s,
            size = Size(3.5.dp.toPx() * crownScale, 8.dp.toPx() * crownScale)
        )
    }

    if (progreso >= 0.85f) {
        val appleScale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val appleR = 6.dp.toPx() * appleScale
        val fruits = listOf(
            Offset(centerX - ovalW * 0.20f, topY + ovalH * 0.42f),
            Offset(centerX + ovalW * 0.18f, topY + ovalH * 0.55f),
            Offset(centerX, topY + ovalH * 0.24f),
            Offset(centerX - ovalW * 0.15f, topY + ovalH * 0.74f)
        )
        fruits.forEach { drawCircle(color = AppleRed, radius = appleR, center = it) }
    }
}

private fun DrawScope.drawFlatPine(
    centerX: Float,
    groundY: Float,
    trunkTopY: Float,
    trunkHeight: Float,
    w: Float,
    crownScale: Float,
    progreso: Float
) {
    drawFlatTaperedTrunk(
        centerX = centerX,
        groundY = groundY,
        topY = trunkTopY - 6.dp.toPx() * crownScale,
        topHalfWidth = 7.dp.toPx() * crownScale,
        baseHalfWidth = 20.dp.toPx() * crownScale
    )

    val baseW = w * 0.41f * crownScale
    val totalPineH = trunkHeight * 1.55f * crownScale
    val bottomY = trunkTopY + 6.dp.toPx() * crownScale

    for (tier in 0..2) {
        val factor = 1f - tier * 0.22f
        val tierW = baseW * factor
        val tierBottom = bottomY - tier * (totalPineH * 0.27f)
        val tierTop = tierBottom - (totalPineH * 0.46f)

        val leftTri = Path().apply {
            moveTo(centerX, tierTop)
            lineTo(centerX - tierW / 2f, tierBottom)
            lineTo(centerX, tierBottom)
            close()
        }
        val rightTri = Path().apply {
            moveTo(centerX, tierTop)
            lineTo(centerX + tierW / 2f, tierBottom)
            lineTo(centerX, tierBottom)
            close()
        }
        drawPath(leftTri, FlatGreenLight)
        drawPath(rightTri, FlatGreenDark)
    }

    val dots = listOf(
        Offset(centerX - baseW * 0.16f, bottomY - totalPineH * 0.12f),
        Offset(centerX + baseW * 0.15f, bottomY - totalPineH * 0.16f),
        Offset(centerX - baseW * 0.11f, bottomY - totalPineH * 0.42f),
        Offset(centerX + baseW * 0.10f, bottomY - totalPineH * 0.48f),
        Offset(centerX - baseW * 0.06f, bottomY - totalPineH * 0.72f)
    )
    dots.forEach { d ->
        drawCircle(color = FlatGreenDot, radius = 3.dp.toPx() * crownScale, center = d)
    }

    if (progreso >= 0.85f) {
        val appleScale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val r = 6.dp.toPx() * appleScale
        val fruits = listOf(
            Offset(centerX - baseW * 0.22f, bottomY - totalPineH * 0.08f),
            Offset(centerX + baseW * 0.20f, bottomY - totalPineH * 0.12f),
            Offset(centerX - baseW * 0.14f, bottomY - totalPineH * 0.36f),
            Offset(centerX + baseW * 0.14f, bottomY - totalPineH * 0.40f),
            Offset(centerX, bottomY - totalPineH * 0.65f)
        )
        fruits.forEach { drawCircle(color = Color(0xFFFFB300), radius = r, center = it) }
    }
}

private fun DrawScope.drawFlatBonsai(
    centerX: Float,
    groundY: Float,
    trunkTopY: Float,
    trunkHeight: Float,
    w: Float,
    crownScale: Float,
    progreso: Float
) {
    val topCenterY = trunkTopY - 8.dp.toPx() * crownScale
    drawFlatTaperedTrunk(
        centerX = centerX,
        groundY = groundY,
        topY = topCenterY,
        topHalfWidth = 8.dp.toPx() * crownScale,
        baseHalfWidth = 24.dp.toPx() * crownScale
    )

    val branchStartY = trunkTopY + trunkHeight * 0.38f
    val leftEnd = Offset(centerX - w * 0.25f * crownScale, trunkTopY + 10.dp.toPx() * crownScale)
    val rightEnd = Offset(centerX + w * 0.25f * crownScale, trunkTopY + 6.dp.toPx() * crownScale)

    val leftBranch = Path().apply {
        moveTo(centerX - 4.dp.toPx(), branchStartY)
        quadraticTo(
            centerX - w * 0.18f * crownScale,
            branchStartY + 4.dp.toPx(),
            leftEnd.x,
            leftEnd.y
        )
    }
    val rightBranch = Path().apply {
        moveTo(centerX + 4.dp.toPx(), branchStartY - 6.dp.toPx())
        quadraticTo(
            centerX + w * 0.18f * crownScale,
            branchStartY - 2.dp.toPx(),
            rightEnd.x,
            rightEnd.y
        )
    }

    drawPath(
        path = leftBranch,
        color = FlatTrunkLight,
        style = Stroke(width = 7.dp.toPx() * crownScale, cap = StrokeCap.Round)
    )
    drawPath(
        path = rightBranch,
        color = FlatTrunkDark,
        style = Stroke(width = 7.dp.toPx() * crownScale, cap = StrokeCap.Round)
    )

    drawFlatDome(center = Offset(centerX, topCenterY + 4.dp.toPx()), rx = w * 0.22f * crownScale, ry = w * 0.14f * crownScale)
    drawFlatDome(center = Offset(leftEnd.x - 3.dp.toPx(), leftEnd.y + 3.dp.toPx()), rx = w * 0.12f * crownScale, ry = w * 0.075f * crownScale)
    drawFlatDome(center = Offset(rightEnd.x + 3.dp.toPx(), rightEnd.y + 3.dp.toPx()), rx = w * 0.12f * crownScale, ry = w * 0.075f * crownScale)

    if (progreso >= 0.85f) {
        val appleScale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val r = 6.dp.toPx() * appleScale
        val fruits = listOf(
            Offset(centerX - w * 0.09f * crownScale, topCenterY - w * 0.03f * crownScale),
            Offset(centerX + w * 0.08f * crownScale, topCenterY - w * 0.02f * crownScale),
            Offset(leftEnd.x - 3.dp.toPx(), leftEnd.y - 3.dp.toPx()),
            Offset(rightEnd.x + 3.dp.toPx(), rightEnd.y - 3.dp.toPx())
        )
        fruits.forEach { drawCircle(color = AppleRed, radius = r, center = it) }
    }
}

private fun DrawScope.drawFlatDome(center: Offset, rx: Float, ry: Float) {
    val rect = Rect(center.x - rx, center.y - ry, center.x + rx, center.y + ry)
    val leftDome = Path().apply {
        moveTo(center.x - rx, center.y)
        arcTo(rect, 180f, 90f, false)
        lineTo(center.x, center.y)
        close()
    }
    val rightDome = Path().apply {
        moveTo(center.x, center.y - ry)
        arcTo(rect, 270f, 90f, false)
        lineTo(center.x, center.y)
        close()
    }
    drawPath(leftDome, FlatGreenLight)
    drawPath(rightDome, FlatGreenDark)
    drawCircle(color = FlatGreenDot, radius = 2.6.dp.toPx(), center = Offset(center.x - rx * 0.35f, center.y - ry * 0.35f))
    drawCircle(color = FlatGreenDot, radius = 2.6.dp.toPx(), center = Offset(center.x + rx * 0.35f, center.y - ry * 0.30f))
}

private fun DrawScope.drawFlatPalm(
    centerX: Float,
    groundY: Float,
    trunkTopY: Float,
    trunkHeight: Float,
    w: Float,
    crownScale: Float,
    progreso: Float
) {
    drawFlatTaperedTrunk(
        centerX = centerX,
        groundY = groundY,
        topY = trunkTopY,
        topHalfWidth = 5.dp.toPx() * crownScale,
        baseHalfWidth = 17.dp.toPx() * crownScale
    )

    val numRings = 5
    for (i in 1..numRings) {
        val t = i.toFloat() / (numRings + 1)
        val ry = trunkTopY + trunkHeight * t
        val hw = (5.dp.toPx() + 9.dp.toPx() * t) * crownScale
        drawLine(
            color = FlatTrunkDark.copy(alpha = 0.55f),
            start = Offset(centerX - hw, ry),
            end = Offset(centerX + hw, ry),
            strokeWidth = 2.dp.toPx()
        )
    }

    val span = w * 0.25f * crownScale
    drawPalmFrond(Offset(centerX, trunkTopY), Offset(centerX - span, trunkTopY + span * 0.35f), -span * 0.45f, FlatGreenLight, FlatGreenDark)
    drawPalmFrond(Offset(centerX, trunkTopY), Offset(centerX + span, trunkTopY + span * 0.35f), -span * 0.45f, FlatGreenLight, FlatGreenDark)

    drawPalmFrond(Offset(centerX, trunkTopY), Offset(centerX - span * 0.72f, trunkTopY - span * 0.42f), -span * 0.35f, FlatGreenLight, FlatGreenDark)
    drawPalmFrond(Offset(centerX, trunkTopY), Offset(centerX + span * 0.72f, trunkTopY - span * 0.42f), -span * 0.35f, FlatGreenLight, FlatGreenDark)

    drawPalmFrond(Offset(centerX, trunkTopY), Offset(centerX - span * 0.55f, trunkTopY + span * 0.68f), -span * 0.22f, FlatGreenLight, FlatGreenDark)
    drawPalmFrond(Offset(centerX, trunkTopY), Offset(centerX + span * 0.55f, trunkTopY + span * 0.68f), -span * 0.22f, FlatGreenLight, FlatGreenDark)

    if (progreso >= 0.85f) {
        val scale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val r = 6.dp.toPx() * scale
        drawCircle(color = Color(0xFF5D4037), radius = r, center = Offset(centerX - 6.dp.toPx(), trunkTopY + 8.dp.toPx()))
        drawCircle(color = Color(0xFF4E342E), radius = r, center = Offset(centerX + 6.dp.toPx(), trunkTopY + 8.dp.toPx()))
        drawCircle(color = Color(0xFF6D4C41), radius = r, center = Offset(centerX, trunkTopY + 12.dp.toPx()))
    }
}

private fun DrawScope.drawPalmFrond(
    start: Offset,
    end: Offset,
    archY: Float,
    topColor: Color,
    bottomColor: Color
) {
    val midX = (start.x + end.x) / 2f
    val midY = (start.y + end.y) / 2f + archY

    val upper = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(midX, midY - 9.dp.toPx(), end.x, end.y)
        quadraticTo(midX, midY, start.x, start.y)
        close()
    }
    val lower = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(midX, midY, end.x, end.y)
        quadraticTo(midX, midY + 8.dp.toPx(), start.x, start.y)
        close()
    }
    drawPath(upper, topColor)
    drawPath(lower, bottomColor)
}

private fun DrawScope.drawFlatFlowerTree(
    centerX: Float,
    groundY: Float,
    trunkTopY: Float,
    w: Float,
    crownScale: Float,
    progreso: Float
) {
    drawFlatTaperedTrunk(
        centerX = centerX,
        groundY = groundY,
        topY = trunkTopY - 10.dp.toPx() * crownScale,
        topHalfWidth = 9.dp.toPx() * crownScale,
        baseHalfWidth = 24.dp.toPx() * crownScale
    )

    val sakuraLight = Color(0xFFF8BBD0)
    val sakuraDark = Color(0xFFF48FB1)
    val sakuraDot = Color(0xFFEC407A)

    val r = w * 0.22f * crownScale
    val cy = trunkTopY - r * 0.65f
    val lobeDist = r * 0.56f
    val lobeR = r * 0.56f

    val angles = listOf(-90.0, -18.0, 54.0, 126.0, 198.0)

    angles.forEach { deg ->
        val rad = Math.toRadians(deg)
        val lx = centerX + (cos(rad) * lobeDist).toFloat()
        val ly = cy + (sin(rad) * lobeDist).toFloat()
        drawCircle(color = sakuraLight, radius = lobeR, center = Offset(lx, ly))
    }
    drawCircle(color = sakuraLight, radius = r * 0.65f, center = Offset(centerX, cy))

    clipRect(left = centerX, top = cy - r * 1.4f, right = centerX + r * 1.4f, bottom = cy + r * 1.4f) {
        angles.forEach { deg ->
            val rad = Math.toRadians(deg)
            val lx = centerX + (cos(rad) * lobeDist).toFloat()
            val ly = cy + (sin(rad) * lobeDist).toFloat()
            drawCircle(color = sakuraDark, radius = lobeR, center = Offset(lx, ly))
        }
        drawCircle(color = sakuraDark, radius = r * 0.65f, center = Offset(centerX, cy))
    }

    angles.forEach { deg ->
        val rad = Math.toRadians(deg)
        val lx = centerX + (cos(rad) * lobeDist * 0.85f).toFloat()
        val ly = cy + (sin(rad) * lobeDist * 0.85f).toFloat()
        drawCircle(color = sakuraDot, radius = 3.4.dp.toPx() * crownScale, center = Offset(lx, ly))
    }

    if (progreso >= 0.85f) {
        val appleScale = ((progreso - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val appleR = 6.dp.toPx() * appleScale
        val fruits = listOf(
            Offset(centerX - r * 0.52f, cy - r * 0.15f),
            Offset(centerX + r * 0.52f, cy - r * 0.15f),
            Offset(centerX, cy - r * 0.58f),
            Offset(centerX - r * 0.34f, cy + r * 0.45f),
            Offset(centerX + r * 0.34f, cy + r * 0.45f)
        )
        fruits.forEach { drawCircle(color = Color(0xFFD81B60), radius = appleR, center = it) }
    }
}

private fun DrawScope.drawFlower(center: Offset, petalColor: Color, centerColor: Color) {
    drawLine(
        color = Color(0xFF2E7D32),
        start = center,
        end = Offset(center.x, center.y + 10.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )
    val petalDist = 4.dp.toPx()
    for (i in 0 until 5) {
        val angle = Math.toRadians((i * 72).toDouble())
        val px = center.x + (cos(angle) * petalDist).toFloat()
        val py = center.y + (sin(angle) * petalDist).toFloat()
        drawCircle(color = petalColor, radius = 3.dp.toPx(), center = Offset(px, py))
    }
    drawCircle(color = centerColor, radius = 2.5.dp.toPx(), center = center)
}

private fun DrawScope.drawTulip(center: Offset, color: Color) {
    drawLine(
        color = Color(0xFF2E7D32),
        start = center,
        end = Offset(center.x, center.y + 12.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )
    drawOval(
        color = color,
        topLeft = Offset(center.x - 4.dp.toPx(), center.y - 6.dp.toPx()),
        size = Size(8.dp.toPx(), 10.dp.toPx())
    )
}

private fun DrawScope.drawLavender(center: Offset) {
    drawLine(
        color = Color(0xFF2E7D32),
        start = center,
        end = Offset(center.x, center.y + 12.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )
    for (i in 0..2) {
        drawCircle(
            color = Color(0xFF9575CD),
            radius = 2.5.dp.toPx(),
            center = Offset(center.x, center.y - (i * 3).dp.toPx())
        )
    }
}

private fun DrawScope.drawMushroom(center: Offset, scale: Float) {
    drawRoundRect(
        color = Color(0xFFFFF8E1),
        topLeft = Offset(center.x - 3.dp.toPx() * scale, center.y),
        size = Size(6.dp.toPx() * scale, 9.dp.toPx() * scale),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawOval(
        color = Color(0xFFE53935),
        topLeft = Offset(center.x - 8.dp.toPx() * scale, center.y - 6.dp.toPx() * scale),
        size = Size(16.dp.toPx() * scale, 9.dp.toPx() * scale)
    )
    drawCircle(
        color = Color.White,
        radius = 1.5.dp.toPx() * scale,
        center = Offset(center.x - 3.dp.toPx() * scale, center.y - 2.dp.toPx() * scale)
    )
    drawCircle(
        color = Color.White,
        radius = 1.5.dp.toPx() * scale,
        center = Offset(center.x + 3.dp.toPx() * scale, center.y - 2.dp.toPx() * scale)
    )
}

private fun DrawScope.drawCloud(
    center: Offset,
    scale: Float
) {
    val cloudColor = Color.White.copy(alpha = 0.85f)
    val r = 18.dp.toPx() * scale
    drawCircle(color = cloudColor, radius = r, center = center)
    drawCircle(color = cloudColor, radius = r * 0.8f, center = Offset(center.x - r * 0.95f, center.y + r * 0.15f))
    drawCircle(color = cloudColor, radius = r * 0.85f, center = Offset(center.x + r * 0.95f, center.y + r * 0.12f))
}