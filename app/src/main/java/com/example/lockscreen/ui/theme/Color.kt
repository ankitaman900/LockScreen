package com.example.lockscreen.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * An original palette inspired by the *look and feel* of modern Android
 * lock screens: deep midnight blues melting into violet, with a cyan accent.
 * No vendor artwork, logo or proprietary asset is used anywhere in this app.
 */

// Wallpaper gradient stops (top -> bottom).
val NightTop = Color(0xFF070B1E)
val NightMid = Color(0xFF1A1140)
val NightGlow = Color(0xFF3C1B56)
val NightBottom = Color(0xFF06040F)

// Soft coloured light blooms painted on top of the gradient.
val BloomCyan = Color(0xFF3FC8FF)
val BloomMagenta = Color(0xFFB44BFF)
val BloomAmber = Color(0xFFFF9A6C)

// Foreground.
val Ink = Color(0xFFFFFFFF)
val InkMuted = Color(0xB3FFFFFF)   // 70 % white
val InkFaint = Color(0x66FFFFFF)   // 40 % white

// Frosted "glass" surfaces for the keypad buttons.
val GlassIdle = Color(0x1FFFFFFF)
val GlassPressed = Color(0x52FFFFFF)
val GlassStroke = Color(0x2EFFFFFF)
