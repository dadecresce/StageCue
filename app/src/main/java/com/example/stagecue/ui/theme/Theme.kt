package com.example.stagecue.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Palette di default (vuote, useremo dynamic quando disponibile)
private val DarkColors = darkColorScheme()
private val LightColors = lightColorScheme()

@Composable
fun StageCueTheme(
    useDarkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && useDarkTheme ->
            dynamicDarkColorScheme(context as Activity)
        dynamicColor && !useDarkTheme ->
            dynamicLightColorScheme(context as Activity)
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
