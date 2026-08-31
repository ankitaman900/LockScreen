package com.example.lockscreen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * A lock screen is always dark, so the app deliberately uses one dark scheme
 * regardless of the system setting (the parameter is kept for completeness and
 * so the Compose previews behave predictably).
 */
private val LockColorScheme = darkColorScheme(
    primary = BloomCyan,
    onPrimary = NightTop,
    background = NightTop,
    onBackground = Ink,
    surface = NightMid,
    onSurface = Ink
)

@Composable
fun LockScreenTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LockColorScheme,
        typography = LockTypography,
        content = content
    )
}
