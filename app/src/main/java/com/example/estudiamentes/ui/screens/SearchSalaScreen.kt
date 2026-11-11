
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
import com.example.estudiamentes.data.GrupoEncontrado
import com.example.estudiamentes.data.buscarGrupoPorCodigo
import com.example.estudiamentes.data.unirseAGrupo

@Composable
fun SearchSalaScreen(
    onBack: () -> Unit = {},
    onSalaUnida: (String) -> Unit = {}
) {
    var codigo by remember {
        mutableStateOf(List(6) { "" })
    }

    val focusRequesters = remember {
        List(6) { FocusRequester() }
    }

    val codigoCompleto = codigo.all { it.isNotEmpty() }

    var buscando by remember { mutableStateOf(false) }
    var uniendose by remember { mutableStateOf(false) }

    var grupoEncontrado by remember {
        mutableStateOf<GrupoEncontrado?>(null)
    }

    var errorMensaje by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp)
    ) {

        // ENCABEZADO
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

        // CAMPOS DEL CÓDIGO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            codigo.forEachIndexed { index, numero ->

                BasicTextField(
                    value = numero,
                    onValueChange = { nuevoValor ->

                        val digito = nuevoValor
                            .filter { it.isDigit() }
                            .take(1)

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
                        .background(
                            Color.White,
                            RoundedCornerShape(12.dp)
                        )
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
                    enabled = !buscando && !uniendose,
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

        // BOTÓN CONTINUAR
        Button(
            onClick = {
                buscando = true
                errorMensaje = null

                buscarGrupoPorCodigo(
                    codigo = codigo.joinToString(""),

                    onSuccess = { grupo ->
                        buscando = false
                        grupoEncontrado = grupo
                    },

                    onError = { error ->
                        buscando = false
                        errorMensaje = error.localizedMessage
                            ?: "No se pudo buscar la sala."
                    }
                )
            },
            enabled = codigoCompleto && !buscando && !uniendose,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64B5F6),
                disabledContainerColor = Color(0xFFBBDEFB)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (buscando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando sala...")
            } else {
                Text(
                    text = "Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // MENSAJE DE ERROR
        errorMensaje?.let { mensaje ->
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // DIÁLOGO PARA CONFIRMAR UNIÓN
    grupoEncontrado?.let { grupo ->

        AlertDialog(
            onDismissRequest = {
                if (!uniendose) {
                    grupoEncontrado = null
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,

            title = {
                Text(
                    text = "¡Sala encontrada!",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF64B5F6),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },

            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¿Quieres unirte a la sala?",
                        fontSize = 16.sp,
                        color = Color(0xFF424242)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = grupo.nombre,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263449),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (grupo.privado) "Sala privada" else "Sala pública",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    if (uniendose) {
                        Spacer(modifier = Modifier.height(16.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = Color(0xFF64B5F6)
                        )

                        Text(
                            text = "Uniéndote a la sala...",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        uniendose = true

                        unirseAGrupo(
                            groupId = grupo.groupId,

                            onSuccess = {
                                uniendose = false
                                grupoEncontrado = null
                                onSalaUnida(grupo.groupId)
                            },

                            onError = { error ->
                                uniendose = false
                                grupoEncontrado = null
                                errorMensaje = error.localizedMessage
                                    ?: "No se pudo entrar en la sala."
                            }
                        )
                    },
                    enabled = !uniendose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF64B5F6)
                    )
                ) {
                    Text(
                        text = "Aceptar",
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        grupoEncontrado = null
                    },
                    enabled = !uniendose
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.Gray
                    )
                }
            }
        )
    }
}
