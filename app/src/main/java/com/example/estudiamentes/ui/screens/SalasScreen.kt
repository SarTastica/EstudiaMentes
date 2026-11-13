package com.example.estudiamentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.model.SalaModel
import com.google.firebase.firestore.FirebaseFirestore

val BluePrimary = Color(0xFF5A9BFF)
val BlueLight = Color(0xFFE8F1FF)
val TextGray = Color(0xFF8A9EBF)
val TextDark = Color(0xFF1A1F36)

@Composable
fun SalasScreen(
    paddingValues: PaddingValues,
    onBuscarSala: () -> Unit,
    onCrearSala: () -> Unit
) {
    var salasList by remember { mutableStateOf<List<SalaModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups")
            .whereEqualTo("isPrivate", false)
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    salasList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(SalaModel::class.java)?.copy(id = doc.id)
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Salas",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BluePrimary
            )

            IconButton(
                onClick = onCrearSala,
                modifier = Modifier
                    .size(48.dp)
                    .background(BlueLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear sala",
                    tint = BluePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clickable { onBuscarSala() }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar por asignatura o sala...", color = TextGray)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextDark)
                },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color(0xFFD1DDF2),
                    disabledContainerColor = Color.White,
                    disabledPlaceholderColor = TextGray
                ),
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            salasList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay salas públicas disponibles.",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(salasList) { sala ->
                        SalaCardItem(
                            sala = sala,
                            onEntrarClick = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SalaCardItem(sala: SalaModel, onEntrarClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(BluePrimary.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sala.name.ifEmpty { "Grupo sin nombre" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Asignatura general",
                        fontSize = 12.sp,
                        color = BluePrimary
                    )
                }

                Row {
                    Box(modifier = Modifier.size(30.dp).background(BluePrimary, CircleShape))
                    Box(modifier = Modifier.size(30.dp).offset(x = (-10).dp).background(Color(0xFF34C759), CircleShape))
                    Box(modifier = Modifier.size(30.dp).offset(x = (-20).dp).background(Color(0xFFFF9500), CircleShape))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TagItem("General")
                    TagItem("Apuntes")
                }

                Button(
                    onClick = onEntrarClick,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Entrar", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TagItem(text: String) {
    Box(
        modifier = Modifier
            .background(BlueLight, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = BluePrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}