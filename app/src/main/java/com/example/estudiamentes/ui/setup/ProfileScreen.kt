
package com.example.estudiamentes.ui.setup

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val Blue = Color(0xFF64B5F6)
private val DarkBlue = Color(0xFF1E88E5)
private val LightBlue = Color(0xFFE2F4FF)

//TODO cuando tengamos el temporizador y se guarden los tiempos el una colección, hacer que la gráfica muestre los datos reales
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit = {},
) {
    var username by remember { mutableStateOf("Cargando...") }
    var photoUrl by remember { mutableStateOf<String?>(null) }


    // Recuperar los datos del usuario desde Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: "Usuario"
                        photoUrl = document.getString("foto")
                    } else {
                        username = "Usuario"
                    }
                }
                .addOnFailureListener {
                    username = "Usuario"
                }
        } else {
            username = "Usuario"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Título
        Text(
            text = "Perfil",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Línea horizontal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(LightBlue)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón editar perfil
        Text(
            text = "Editar perfil",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Transparent)
                .clickable { onEditProfile() }
                .padding(4.dp)
        )

        // Avatar y nombre de usuario
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
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto de perfil predeterminada",
                        modifier = Modifier.size(75.dp),
                        tint = DarkBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = username,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Estadísticas
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Horas de estudio en la semana",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )

                Spacer(modifier = Modifier.height(20.dp))

                WeeklyStudyChart(
                    dailyHours = listOf(
                        2f, 3.5f, 1.5f, 3f, 2.5f, 4f, 1f
                    ),
                    dailyGoal = 4f
                )
            }
        }
    }
}

@Composable
fun WeeklyStudyChart(
    dailyHours: List<Float>,
    dailyGoal: Float
) {
    val days = listOf("L", "M", "X", "J", "V", "S", "D")

    val maxHours = dailyGoal.coerceAtLeast(1f)
    val numberOfHours = kotlin.math.floor(maxHours).toInt()

    val chartColor = Color(0xFF1E88E5)
    val notebookBlue = Color(0xFFD7EDFF)
    val pointYellow = Color(0xFFFFF9A1)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            // Eje Y: números de las horas
            // Eje Y: números alineados con las líneas
            Box(
                modifier = Modifier
                    .width(30.dp)
                    .fillMaxHeight()
            ) {
                for (hour in 0..numberOfHours) {
                    val yPosition = 1f - (hour / maxHours)

                    Text(
                        text = hour.toString(),
                        fontSize = 11.sp,
                        color = chartColor,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                y = 210.dp * yPosition - 8.dp
                            )
                    )
                }
            }

            // Gráfica
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val rightSpace = 12.dp.toPx()
                val chartWidth = size.width - rightSpace
                val chartHeight = size.height

                // Líneas horizontales
                for (hour in 0..numberOfHours) {
                    val y = chartHeight * (1f - hour / maxHours)

                    drawLine(
                        color = notebookBlue,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // Puntos de la gráfica
                val points = dailyHours.take(7).mapIndexed { index, hours ->
                    val x = chartWidth * index / 6f
                    val y = chartHeight *
                            (1f - hours.coerceIn(0f, maxHours) / maxHours)

                    Offset(x, y)
                }

                // Línea azul que conecta los puntos
                val path = Path().apply {
                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            moveTo(point.x, point.y)
                        } else {
                            lineTo(point.x, point.y)
                        }
                    }
                }

                if (points.size > 1) {
                    drawPath(
                        path = path,
                        color = chartColor,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }

                // Puntos amarillos
                points.forEach { point ->
                    drawCircle(
                        color = pointYellow,
                        radius = 6.dp.toPx(),
                        center = point
                    )

                    drawCircle(
                        color = chartColor,
                        radius = 6.dp.toPx(),
                        center = point,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }

        // Eje X: días de la semana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    color = chartColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
