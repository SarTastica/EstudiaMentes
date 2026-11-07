package com.example.estudiamentes.ui.setup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileSetupScreen(
    onFinish: () -> Unit = {}
) {
    var currentStep by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var studyLevel by remember { mutableStateOf("") }
    var center by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }

    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var studyHoursPerDay by remember { mutableFloatStateOf(2f) }
    var hourInput by remember { mutableStateOf("") }
    var minuteInput by remember { mutableStateOf("") }
    var timeError by remember { mutableStateOf<String?>(null) }

    var mainGoal by remember { mutableStateOf("") }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE2F4FF), Color.White)
    )

    fun validateTime(): Boolean {
        val h = hourInput.toIntOrNull() ?: -1
        val m = minuteInput.toIntOrNull() ?: -1

        if (hourInput.isEmpty() || minuteInput.isEmpty()) {
            timeError = "Por favor, completa la hora y los minutos"
            return false
        }

        if (h !in 0..23 || m !in 0..59) {
            timeError = "Hora inválida. Debe ser entre 00:00 y 23:59"
            return false
        }

        timeError = null
        return true
    }

    Box(
        modifier = Modifier.fillMaxSize().background(brush = backgroundGradient).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            // Indicador de progreso (4 pasos)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier.size(24.dp).background(
                            color = if (i == currentStep) Color(0xFF64B5F6) else Color.Transparent,
                            shape = CircleShape
                        ).border(1.dp, Color(0xFF64B5F6), CircleShape)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (currentStep) {
                        1 -> StepOne(name, { name = it }, username, { username = it })
                        2 -> StepTwo(
                            studyLevel = studyLevel, onStudyLevelChange = { studyLevel = it },
                            center = center, onCenterChange = { center = it },
                            branch = branch, onBranchChange = { branch = it }
                        )
                        3 -> StepThree(
                            selectedDays = selectedDays,
                            onDayToggle = { day ->
                                selectedDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
                            },
                            hours = studyHoursPerDay,
                            onHoursChange = { studyHoursPerDay = it },
                            hourInput = hourInput,
                            onHourChange = { if (it.length <= 2) hourInput = it },
                            minuteInput = minuteInput,
                            onMinuteChange = { if (it.length <= 2) minuteInput = it },
                            timeError = timeError
                        )
                        4 -> StepFour(mainGoal, { mainGoal = it })
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (currentStep == 1) Arrangement.End else Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64B5F6))
                    ) { Text("atrás", fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6)) }
                }

                Button(
                    onClick = {
                        if (currentStep == 3) {
                            if (!validateTime()) return@Button
                        }
                        if (currentStep < 4) currentStep++ else onFinish()
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
                ) { Text(if (currentStep == 4) "finalizar" else "siguiente", fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}

@Composable
fun StepOne(name: String, onNameChange: (String) -> Unit, username: String, onUsernameChange: (String) -> Unit) {
    FormTextField(label = "¿Cómo te llamas?", value = name, onValueChange = onNameChange)
    Spacer(modifier = Modifier.height(32.dp))
    FormTextField(label = "Nombre de usuario", subtitle = "Así podrán encontrarte", value = username, onValueChange = onUsernameChange)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTwo(
    studyLevel: String, onStudyLevelChange: (String) -> Unit,
    center: String, onCenterChange: (String) -> Unit,
    branch: String, onBranchChange: (String) -> Unit
) {
    var expandedLevel by remember { mutableStateOf(false) }
    val levels = listOf("Primaria", "ESO", "Bachillerato", "Universidad", "Ciclo Formativo", "Trabajo propio")

    var searchResults by remember { mutableStateOf(listOf<String>()) }
    var isCenterExpanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        Text(text = "Nivel de estudios", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedLevel,
            onExpandedChange = { expandedLevel = !expandedLevel },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = studyLevel,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Selecciona tu nivel", color = Color(0xFF90CAF9)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLevel) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF90CAF9), focusedBorderColor = Color(0xFF42A5F5),
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expandedLevel,
                onDismissRequest = { expandedLevel = false },
                modifier = Modifier.background(Color.White)
            ) {
                levels.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level, color = Color.DarkGray) },
                        onClick = {
                            onStudyLevelChange(level)
                            expandedLevel = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "¿Cuál es tu centro?", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Text(text = "Escribe para buscar en la nube", color = Color(0xFF90CAF9), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = center,
                onValueChange = { newValue ->
                    onCenterChange(newValue)
                    searchCenters(newValue) { results ->
                        searchResults = results
                        isCenterExpanded = results.isNotEmpty()
                    }
                },
                placeholder = { Text("Ej. IES Carrizal, ULPGC...", color = Color(0xFF90CAF9)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFF90CAF9), focusedBorderColor = Color(0xFF42A5F5),
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White
                )
            )

            DropdownMenu(
                expanded = isCenterExpanded,
                onDismissRequest = { isCenterExpanded = false },
                modifier = Modifier.fillMaxWidth(0.85f).background(Color.White),
                properties = androidx.compose.ui.window.PopupProperties(focusable = false)
            ) {
                searchResults.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion, color = Color.DarkGray) },
                        onClick = {
                            onCenterChange(suggestion)
                            isCenterExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        FormTextField(label = "¿Cuál es tu rama?", subtitle = "Ej. Ingeniería, Ciencias...", value = branch, onValueChange = onBranchChange)
    }
}

@Composable
fun StepThree(
    selectedDays: Set<String>, onDayToggle: (String) -> Unit,
    hours: Float, onHoursChange: (Float) -> Unit,
    hourInput: String, onHourChange: (String) -> Unit,
    minuteInput: String, onMinuteChange: (String) -> Unit,
    timeError: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        Text("¿Qué días estudias?", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        val daysOfWeek = listOf("L", "M", "X", "J", "V", "S", "D")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            daysOfWeek.forEach { day ->
                val isSelected = selectedDays.contains(day)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = if (isSelected) Color(0xFF42A5F5) else Color(0xFFE3F2FD), shape = CircleShape)
                        .clickable { onDayToggle(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = day, color = if (isSelected) Color.White else Color(0xFF1E88E5), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Horas por día", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("${hours.toInt()} horas", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E88E5))
        Slider(
            value = hours, onValueChange = onHoursChange, valueRange = 1f..24f, steps = 22,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF42A5F5), activeTrackColor = Color(0xFF90CAF9))
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("¿A qué hora estudias?", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Text("Introduce formato 24h (00:00 a 23:59)", color = Color(0xFF90CAF9), fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = hourInput,
                onValueChange = onHourChange,
                placeholder = { Text("HH", color = Color(0xFF90CAF9)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(75.dp).height(60.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFF90CAF9), focusedBorderColor = Color(0xFF42A5F5)
                )
            )

            Text(
                text = ":",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64B5F6),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            OutlinedTextField(
                value = minuteInput,
                onValueChange = onMinuteChange,
                placeholder = { Text("MM", color = Color(0xFF90CAF9)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(75.dp).height(60.dp),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFF90CAF9), focusedBorderColor = Color(0xFF42A5F5)
                )
            )
        }

        if (timeError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = timeError, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StepFour(selectedGoal: String, onGoalChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("¿Cuál es tu objetivo principal?", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        val goals = listOf("Aprobar el curso", "Crear un hábito diario", "Preparar Oposiciones", "Productividad personal")

        goals.forEach { goal ->
            val isSelected = selectedGoal == goal
            Button(
                onClick = { onGoalChange(goal) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(0xFF42A5F5) else Color(0xFFE3F2FD),
                    contentColor = if (isSelected) Color.White else Color(0xFF1E88E5)
                )
            ) {
                Text(goal, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    subtitle: String? = null,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(text = subtitle, color = Color(0xFF90CAF9), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF90CAF9),
                focusedBorderColor = Color(0xFF42A5F5),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            trailingIcon = if (trailingIcon) {
                { Text("▼", color = Color(0xFF90CAF9), modifier = Modifier.padding(end = 12.dp)) }
            } else null
        )
    }
}

fun searchCenters(query: String, onResult: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val cleanQuery = query.trim().lowercase()

    if (cleanQuery.isEmpty()) {
        onResult(emptyList())
        return
    }

    db.collection("centers")
        .orderBy("searchKey")
        .startAt(cleanQuery)
        .endAt(cleanQuery + "\uf8ff")
        .limit(5)
        .get()
        .addOnSuccessListener { documents ->
            val centerList = documents.map { it.getString("name") ?: "" }
            onResult(centerList)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}

@Preview(showBackground = true)
@Composable
fun ProfileSetupScreenPreview() {
    ProfileSetupScreen()
}