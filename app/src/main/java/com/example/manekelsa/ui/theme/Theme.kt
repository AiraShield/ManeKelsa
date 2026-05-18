package com.example.manekelsa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

// Dark Theme (optional for now)


// ✅ Your Custom Light Theme
private val LightColors = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface
)

@Composable
fun ManeKelsaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // ❗ disable for consistency
    content: @Composable () -> Unit
) {

    val colorScheme = LightColors

    MaterialTheme(
        colorScheme = colorScheme,   // ✅ FIXED
        typography = Typography,
        content = content
    )
}