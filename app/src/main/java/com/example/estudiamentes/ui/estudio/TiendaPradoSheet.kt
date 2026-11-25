package com.example.estudiamentes.ui.estudio

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.estudiamentes.data.ProfileRepository

private val BluePrimary = Color(0xFF5A9BFF)
private val BlueLight = Color(0xFFE8F1FF)
private val TextGray = Color(0xFF6E829E)
private val TextDark = Color(0xFF1A1F36)
private val TreeGreenDark = Color(0xFF2E7D32)
private val AppleRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiendaPradoSheet(
    manzanas: Int,
    itemsDesbloqueados: List<String>,
    itemsEquipados: List<String>,
    profileRepository: ProfileRepository,
    onActualizarEstado: (nuevoSaldo: Int, nuevosDesb: List<String>, nuevosEquip: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var seccionSeleccionada by remember { mutableStateOf("FLORES") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tienda de Mi Prado",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = "Personaliza tu árbol y zona de estudio",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = BlueLight
                ) {
                    Text(
                        text = "🍎 $manzanas",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = TextDark,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                seccionesTienda.forEach { seccion ->
                    val seleccionada = seccion.id == seccionSeleccionada
                    Surface(
                        onClick = { seccionSeleccionada = seccion.id },
                        shape = RoundedCornerShape(50),
                        color = if (seleccionada) BluePrimary else BlueLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = seccion.icono, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = seccion.titulo,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (seleccionada) Color.White else TextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val itemsFiltrados = catalogoZonaEstudio.filter { it.categoria == seccionSeleccionada }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(320.dp)
            ) {
                items(itemsFiltrados) { item ->
                    val desbloqueado = item.esDefault || itemsDesbloqueados.contains(item.id)
                    val tieneOtroDeCategoria = itemsEquipados.any { eqId ->
                        val cat = catalogoZonaEstudio.find { it.id == eqId }?.categoria
                        cat == item.categoria && eqId != item.id
                    }
                    val equipado = if (item.esDefault) {
                        itemsEquipados.contains(item.id) || !tieneOtroDeCategoria
                    } else {
                        itemsEquipados.contains(item.id)
                    }

                    Surface(
                        onClick = {
                            if (desbloqueado) {
                                val nuevosEquipados = if (item.categoria == "PALETAS" || item.categoria == "ARBOLES") {
                                    val sinMismaCategoria = itemsEquipados.filterNot { eqId ->
                                        catalogoZonaEstudio.find { it.id == eqId }?.categoria == item.categoria
                                    }
                                    if (item.esDefault) sinMismaCategoria else sinMismaCategoria + item.id
                                } else {
                                    if (equipado) itemsEquipados - item.id else itemsEquipados + item.id
                                }

                                onActualizarEstado(manzanas, itemsDesbloqueados, nuevosEquipados)
                                profileRepository.guardarPersonalizacionHuerto(
                                    context = context,
                                    manzanas = manzanas,
                                    desbloqueados = itemsDesbloqueados,
                                    equipados = nuevosEquipados
                                )
                            } else if (manzanas >= item.precio) {
                                val nuevoSaldo = manzanas - item.precio
                                val nuevosDesb = itemsDesbloqueados + item.id
                                val filtrados = if (item.categoria == "PALETAS" || item.categoria == "ARBOLES") {
                                    itemsEquipados.filterNot { eqId ->
                                        catalogoZonaEstudio.find { it.id == eqId }?.categoria == item.categoria
                                    }
                                } else {
                                    itemsEquipados
                                }
                                val nuevosEquip = filtrados + item.id

                                onActualizarEstado(nuevoSaldo, nuevosDesb, nuevosEquip)
                                profileRepository.guardarPersonalizacionHuerto(
                                    context = context,
                                    manzanas = nuevoSaldo,
                                    desbloqueados = nuevosDesb,
                                    equipados = nuevosEquip
                                )
                                Toast.makeText(
                                    context,
                                    "¡Has añadido ${item.nombre} a tu prado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Te faltan ${item.precio - manzanas} 🍎",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (equipado) BlueLight else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.5.dp,
                            if (equipado) BluePrimary else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = item.icono, fontSize = 30.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            if (desbloqueado) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (equipado) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = BluePrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = if (equipado) "Puesto" else if (item.esDefault) "Seleccionar" else "Guardar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (equipado) BluePrimary else TextGray
                                    )
                                }
                            } else {
                                Text(
                                    text = "🍎 ${item.precio}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (manzanas >= item.precio) TreeGreenDark else AppleRed
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}