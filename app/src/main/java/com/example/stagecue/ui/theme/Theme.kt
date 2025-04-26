package com.example.stagecue.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// ----------------------------------------------------
// 1) Schemi statici di fallback per API < 31
// ----------------------------------------------------
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Purple40Contrast,
    secondary = Teal40,
    onSecondary = Teal40Contrast,
    background = BackgroundLight,
    onBackground = ContentLight,
    surface = SurfaceLight,
    onSurface = ContentLight
    // aggiungi altri colori se li hai definiti in Color.kt
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple80Contrast,
    secondary = Teal80,
    onSecondary = Teal80Contrast,
    background = BackgroundDark,
    onBackground = ContentDark,
    surface = SurfaceDark,
    onSurface = ContentDark
    // personalizza come ti serve
)

// ----------------------------------------------------
// 2) Tema principale
// ----------------------------------------------------
@Composable
fun StageCueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // abilita o disabilita il dynamic color (Material You)
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Solo su Android 12+ e se abilitato dall'utente
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // fallback statico
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
