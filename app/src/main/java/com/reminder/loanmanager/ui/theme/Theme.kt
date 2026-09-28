package com.reminder.loanmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D5B),
    onPrimary = Color.White,
    secondary = Color(0xFFFFA000),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    error = Color(0xFFD64545)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6FCF9F),
    onPrimary = Color(0xFF00382A),
    secondary = Color(0xFFFFC04D),
    background = Color(0xFF101410),
    surface = Color(0xFF1A1F1A),
    error = Color(0xFFFF8A80)
)

@Composable
fun LoanManagerTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
