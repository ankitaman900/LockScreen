package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.lockscreen.ui.theme.BloomAmber
import com.example.lockscreen.ui.theme.BloomCyan
import com.example.lockscreen.ui.theme.BloomMagenta
import com.example.lockscreen.ui.theme.NightBottom
import com.example.lockscreen.ui.theme.NightGlow
import com.example.lockscreen.ui.theme.NightMid
import com.example.lockscreen.ui.theme.NightTop
import kotlin.math.sin
import kotlin.random.Random

/** One twinkling speck of light. Positions are fractions of the canvas (0f..1f). */
private data class Star(
    val xFraction: Float,
    val yFraction: Float,
    val radius: Float,
    val baseAlpha: Float,
    val phase: Float
)

/**
 * A completely procedural wallpaper: a vertical night gradient, three slowly
 * drifting colour blooms, a field of faint stars and a vignette that darkens
 * the edges so the white text stays readable.
 *
 * Because it is drawn rather than loaded, it costs no APK size and looks sharp
 * on every screen density.
 */
@Composable
fun LockWallpaper(modifier: Modifier = Modifier) {

    // A single 0f..1f value that eases back and forth forever; every moving
    // element derives its position from it, so the whole scene breathes slowly.
    val transition = rememberInfiniteTransition(label = "wallpaper")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    // Generated once with a fixed seed so the sky does not reshuffle on recomposition.
    val stars = remember {
        val random = Random(seed = 7)
        List(70) {
            Star(
                xFraction = random.nextFloat(),
                yFraction = random.nextFloat() * 0.7f,
                radius = 0.6f + random.nextFloat() * 1.5f,
                baseAlpha = 0.15f + random.nextFloat() * 0.45f,
                phase = random.nextFloat() * 6.283f
            )
        }
    }

    Canvas(modifier) {
        val width = size.width
        val height = size.height

        // 1. Base gradient.
        drawRect(
            brush = Brush.verticalGradient(
                0.00f to NightTop,
                0.42f to NightMid,
                0.74f to NightGlow,
                1.00f to NightBottom
            )
        )

        // 2. Soft colour blooms (see the `bloom` helper below). Each one is a
        //    radial gradient fading to transparent, nudged around by `drift`.
        bloom(
            color = BloomMagenta,
            cx = width * (0.20f + 0.10f * drift),
            cy = height * (0.22f + 0.04f * drift),
            radius = width * 0.85f,
            alpha = 0.34f
        )
        bloom(
            color = BloomCyan,
            cx = width * (0.92f - 0.14f * drift),
            cy = height * (0.36f - 0.06f * drift),
            radius = width * 0.70f,
            alpha = 0.24f
        )
        bloom(
            color = BloomAmber,
            cx = width * (0.55f + 0.08f * drift),
            cy = height * (0.86f + 0.03f * drift),
            radius = width * 0.62f,
            alpha = 0.16f
        )

        // 3. Stars, each twinkling out of phase with the others.
        stars.forEach { star ->
            val twinkle = 0.65f + 0.35f * sin(star.phase + drift * 6.283f)
            drawCircle(
                color = Color.White.copy(alpha = star.baseAlpha * twinkle),
                radius = star.radius,
                center = Offset(star.xFraction * width, star.yFraction * height)
            )
        }

        // 4. Vignette: keeps the corners dark so the UI stays legible.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                center = Offset(width / 2f, height * 0.42f),
                radius = maxOf(width, height) * 0.78f
            )
        )
    }
}

/**
 * Paints one soft circle of coloured light that fades to fully transparent at
 * its edge – the trick that gives the wallpaper its depth.
 */
private fun DrawScope.bloom(
    color: Color,
    cx: Float,
    cy: Float,
    radius: Float,
    alpha: Float
) {
    val center = Offset(cx, cy)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
