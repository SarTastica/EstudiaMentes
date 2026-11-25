package com.example.estudiamentes.ui.estudio

import android.content.Context

data class PresetCiclo(
    val id: String,
    val nombre: String,
    val estudioMin: Int,
    val descansoMin: Int
)

data class SeccionTienda(
    val id: String,
    val titulo: String,
    val icono: String
)

data class ItemZonaEstudio(
    val id: String,
    val nombre: String,
    val categoria: String,
    val precio: Int,
    val icono: String,
    val esDefault: Boolean = false
)

val seccionesTienda = listOf(
    SeccionTienda("FLORES", "Flores", "🌸"),
    SeccionTienda("ARBOLES", "Árboles", "🌳"),
    SeccionTienda("PALETAS", "Paletas", "🎨"),
    SeccionTienda("DECORACION", "Decoración", "🏮")
)

val catalogoZonaEstudio = listOf(
    ItemZonaEstudio("flores_margaritas", "Margaritas", "FLORES", 40, "🌼"),
    ItemZonaEstudio("flores_tulipanes", "Tulipanes", "FLORES", 85, "🌷"),
    ItemZonaEstudio("flores_lavanda", "Lavanda", "FLORES", 130, "🪻"),
    ItemZonaEstudio("flores_girasoles", "Girasoles", "FLORES", 190, "🌻"),

    ItemZonaEstudio("arbol_default", "Manzano Clásico", "ARBOLES", 0, "🌳", esDefault = true),
    ItemZonaEstudio("arbol_cipres", "Ciprés Esbelto", "ARBOLES", 280, "🌲"),
    ItemZonaEstudio("arbol_pino", "Pino Escalonado", "ARBOLES", 360, "🎄"),
    ItemZonaEstudio("arbol_bonsai", "Bonsái Acampa", "ARBOLES", 480, "🪴"),
    ItemZonaEstudio("arbol_palmera", "Palmera Isla", "ARBOLES", 560, "🌴"),
    ItemZonaEstudio("arbol_sakura", "Copa Flor", "ARBOLES", 650, "🌸"),

    ItemZonaEstudio("paleta_default", "Día Claro", "PALETAS", 0, "☀️", esDefault = true),
    ItemZonaEstudio("paleta_atardecer", "Atardecer", "PALETAS", 250, "🌅"),
    ItemZonaEstudio("paleta_bosque", "Niebla Bosque", "PALETAS", 300, "🌲"),
    ItemZonaEstudio("paleta_pastel", "Primavera", "PALETAS", 380, "🌈"),
    ItemZonaEstudio("paleta_noche", "Noche Estrellas", "PALETAS", 480, "🌙"),

    ItemZonaEstudio("decor_setas", "Setas Bosque", "DECORACION", 110, "🍄"),
    ItemZonaEstudio("decor_libros", "Pila Libros", "DECORACION", 160, "📚"),
    ItemZonaEstudio("decor_valla", "Valla Madera", "DECORACION", 220, "🪵"),
    ItemZonaEstudio("decor_farol", "Farolillo", "DECORACION", 290, "🏮"),
    ItemZonaEstudio("decor_banco", "Banco Estudio", "DECORACION", 360, "🪑")
)

fun cargarPresetsGuardados(context: Context): List<PresetCiclo> {
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

fun guardarPresetEnPreferencias(context: Context, preset: PresetCiclo) {
    val prefs = context.getSharedPreferences("estudiamentes_pomodoro_presets", Context.MODE_PRIVATE)
    prefs.edit()
        .putInt("${preset.id}_estudio", preset.estudioMin)
        .putInt("${preset.id}_descanso", preset.descansoMin)
        .apply()
}