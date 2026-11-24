package com.example.colegio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColegioColorScheme = lightColorScheme(
    primary = TealColegio,
    onPrimary = Color.White,
    primaryContainer = TealFondoSuave,
    onPrimaryContainer = TealOscuro,

    secondary = VerdeBueno,
    onSecondary = Color.White,

    tertiary = NaranjoPendiente,
    onTertiary = Color.White,

    error = RojoMalo,
    onError = Color.White,
    errorContainer = RojoFondo,
    onErrorContainer = RojoMalo,

    background = FondoApp,
    onBackground = TextoPrincipal,

    surface = FondoTarjeta,
    onSurface = TextoPrincipal,
    surfaceVariant = FondoTarjeta,
    onSurfaceVariant = TextoSecundario,
    outline = BordeTarjeta
)

@Composable
fun ColegioTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColegioColorScheme,
        typography = Typography,
        content = content
    )
}