
package com.example.estudiamentes.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchSalaScreen(
    onBack: () -> Unit = {},
    onContinue: (String) -> Unit = {}
) {
    var codigo by remember { mutableStateOf(List(6) { "" }) }

    val focusRequesters = remember {
        List(6) { FocusRequester() }
    }

    val codigoCompleto = codigo.all { it.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
    ) {
        // Flecha y título en la misma línea
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF64B5F6)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Buscar sala",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF64B5F6)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Introduce el código de 6 dígitos para unirte a una sala.",
            fontSize = 16.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            codigo.forEachIndexed { index, numero ->

                BasicTextField(
                    value = numero,
                    onValueChange = { nuevoValor ->
                        val digito = nuevoValor.filter { it.isDigit() }.take(1)

                        if (nuevoValor.isEmpty() || digito.isNotEmpty()) {
                            val nuevoCodigo = codigo.toMutableList()
                            nuevoCodigo[index] = digito
                            codigo = nuevoCodigo

                            if (digito.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            } else if (digito.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .focusRequester(focusRequesters[index])
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFF90CAF9),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF333333)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (numero.isEmpty()) {
                                Text(
                                    text = "_",
                                    fontSize = 24.sp,
                                    color = Color(0xFFBDBDBD)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { onContinue(codigo.joinToString("")) },
            enabled = codigoCompleto,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64B5F6),
                disabledContainerColor = Color(0xFFBBDEFB)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continuar",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
