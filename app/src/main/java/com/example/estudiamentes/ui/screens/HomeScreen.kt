
package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.drawscope.Stroke

private val HomeBackground = Color(0xFFF5F7FA)
private val HomeBlue = Color(0xFF5A9BFF)
private val HomeDark = Color(0xFF1A1F36)
private val HomeMint = Color(0xFF70D4B8)
private val HomeLightBlue = Color(0xFFE8F1FF)

@Composable
fun HomeScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onContinue: () -> Unit = {}
) {
    var username by remember { mutableStateOf("User1") }

    // Obtener el nombre del usuario que ha iniciado sesión.
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    username = document.getString("username")
                        ?.takeIf { it.isNotBlank() }
                        ?: "User1"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. SALUDO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hola, $username",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HomeDark
            )

            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(HomeBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = username.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 2. ESTUDIO DE HOY
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = HomeLightBlue
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Rueda de progreso con tiempo de ejemplo.
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFFC9DFFF),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(
                                width = 11.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        drawArc(
                            color = HomeBlue,
                            startAngle = -90f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(
                                width = 11.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    Text(
                        text = "3h 45 min",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeDark
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estudio de hoy",
                        fontSize = 19.sp,
                        color = HomeDark,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tiempo restante:",
                        fontSize = 12.sp,
                        color = Color(0xFF52627A)
                    )

                    Text(
                        text = "15 minutos",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF304967)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onContinue,
                        shape = RoundedCornerShape(30.dp),
                        contentPadding = PaddingValues(
                            horizontal = 18.dp,
                            vertical = 7.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC9DFFF),
                            contentColor = Color(0xFF1756A9)
                        )
                    ) {
                        Text(
                            text = "Continuar",
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = "📚",
                    fontSize = 32.sp
                )
            }
        }

        // 3. RENDIMIENTO SEMANAL
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rendimiento semanal",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeDark
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(HomeLightBlue)
                            .padding(horizontal = 11.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "18h de 25h (75%)",
                            fontSize = 10.sp,
                            color = Color(0xFF1756A9)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barra de objetivo semanal.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE1F8F8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(HomeMint)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                val days = listOf("L", "M", "X", "J", "V", "S", "D")
                val progress = listOf(0.35f, 0.75f, 0.9f, 1f, 1f, 0.55f, 0.85f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEachIndexed { index, day ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(19.dp)
                                    .height(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2F4FF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .fillMaxHeight(progress[index])
                                        .clip(CircleShape)
                                        .background(
                                            if (index == 0 || index == 5) {
                                                Color(0xFFF7AEB6)
                                            } else {
                                                HomeMint
                                            }
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text = day,
                                fontSize = 12.sp,
                                color = HomeDark
                            )
                        }
                    }
                }
            }
        }

        // 4. TO-DO Y NOTAS

        TodoNotesSection()
    }
}
