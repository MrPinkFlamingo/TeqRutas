package com.example.teqruta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// ── Esquema Claro ─────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary            = AzulPrimario,
    onPrimary          = Blanco,
    primaryContainer   = AzulClaro,
    onPrimaryContainer = AzulPrimario,
    secondary          = AzulSecundario,
    onSecondary        = Blanco,
    background         = GrisClaro,
    onBackground       = GrisOscuro,
    surface            = Blanco,
    onSurface          = GrisOscuro,
    error              = RojoDesviacion,
    onError            = Blanco,
    errorContainer     = RojoClaro,
    onErrorContainer   = RojoDesviacion,
    outline            = GrisBorde,
    outlineVariant     = GrisBorde
)

// ── Esquema Oscuro ────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = AzulPrimarioDark,
    onPrimary        = SuperficieDark,
    secondary        = AzulSecundarioDark,
    onSecondary      = SuperficieDark,
    background       = SuperficieDark,
    onBackground     = Blanco,
    surface          = SuperficieDark2,
    onSurface        = Blanco,
    error            = RojoClaro,
    onError          = RojoDesviacion
)

@Composable
fun TeqRutaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // dynamicColor desactivado: necesitamos colores corporativos consistentes
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}