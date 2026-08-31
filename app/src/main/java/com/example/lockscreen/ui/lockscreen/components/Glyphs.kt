package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.lockscreen.ui.theme.Ink

/**
 * Every icon in this app is drawn by hand with the Compose [Canvas] API.
 *
 * Two reasons:
 *  1. the app ships no image assets at all, so it stays tiny and works offline;
 *  2. nothing is copied from any vendor's system UI – these are original shapes.
 *
 * All of them scale with the [Modifier] size they are given.
 */

/** A padlock: rounded body, arched shackle, small keyhole. */
@Composable
fun LockGlyph(
    modifier: Modifier = Modifier,
    color: Color = Ink
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.11f

        val bodyWidth = w * 0.76f
        val bodyHeight = h * 0.48f
        val bodyLeft = (w - bodyWidth) / 2f
        val bodyTop = h - bodyHeight

        // Body.
        drawRoundRect(
            color = color,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(w * 0.15f, w * 0.15f)
        )

        // Shackle: a half circle sitting on top of two short legs.
        val radius = w * 0.24f
        val shackleCenterY = bodyTop - radius * 0.5f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w / 2f - radius, shackleCenterY - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        listOf(w / 2f - radius, w / 2f + radius).forEach { x ->
            drawLine(
                color = color,
                start = Offset(x, shackleCenterY),
                end = Offset(x, bodyTop + stroke * 0.2f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        // Keyhole – a darker dot punched into the body.
        drawCircle(
            color = Color.Black.copy(alpha = 0.35f),
            radius = w * 0.08f,
            center = Offset(w / 2f, bodyTop + bodyHeight * 0.45f)
        )
    }
}

/** Mobile signal: four bars of increasing height. */
@Composable
fun SignalGlyph(
    modifier: Modifier = Modifier.size(width = 16.dp, height = 11.dp),
    color: Color = Ink
) {
    Canvas(modifier) {
        val bars = 4
        val gap = size.width * 0.11f
        val barWidth = (size.width - gap * (bars - 1)) / bars
        repeat(bars) { index ->
            val barHeight = size.height * (0.34f + 0.66f * index / (bars - 1f))
            drawRoundRect(
                color = color,
                topLeft = Offset(index * (barWidth + gap), size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.35f, barWidth * 0.35f)
            )
        }
    }
}

/** Wi-Fi: three concentric arcs above a dot. */
@Composable
fun WifiGlyph(
    modifier: Modifier = Modifier.size(width = 16.dp, height = 12.dp),
    color: Color = Ink
) {
    Canvas(modifier) {
        val origin = Offset(size.width / 2f, size.height * 0.95f)
        repeat(3) { index ->
            val radius = size.width * (0.20f + 0.24f * index)
            drawArc(
                color = color,
                startAngle = -155f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(origin.x - radius, origin.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = size.width * 0.095f, cap = StrokeCap.Round)
            )
        }
        drawCircle(color = color, radius = size.width * 0.07f, center = origin)
    }
}

/**
 * Battery outline with a fill level.
 *
 * @param level 0f..1f – how much of the battery is filled.
 */
@Composable
fun BatteryGlyph(
    modifier: Modifier = Modifier.size(width = 22.dp, height = 11.dp),
    color: Color = Ink,
    level: Float = 0.82f
) {
    Canvas(modifier) {
        val stroke = size.height * 0.13f
        val bodyWidth = size.width * 0.85f

        drawRoundRect(
            color = color.copy(alpha = 0.55f),
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(bodyWidth - stroke, size.height - stroke),
            cornerRadius = CornerRadius(size.height * 0.34f, size.height * 0.34f),
            style = Stroke(width = stroke)
        )

        // The little positive terminal on the right.
        drawRoundRect(
            color = color.copy(alpha = 0.55f),
            topLeft = Offset(bodyWidth + size.width * 0.04f, size.height * 0.32f),
            size = Size(size.width * 0.07f, size.height * 0.36f),
            cornerRadius = CornerRadius(size.width * 0.03f, size.width * 0.03f)
        )

        val inset = stroke * 2f
        val trackWidth = bodyWidth - inset * 2f
        drawRoundRect(
            color = color,
            topLeft = Offset(inset, inset),
            size = Size(trackWidth * level.coerceIn(0f, 1f), size.height - inset * 2f),
            cornerRadius = CornerRadius(size.height * 0.22f, size.height * 0.22f)
        )
    }
}

/** Backspace: a pointed tag with a small cross inside. */
@Composable
fun BackspaceGlyph(
    modifier: Modifier = Modifier,
    color: Color = Ink
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.07f
        val tipX = w * 0.06f
        val bodyLeft = w * 0.34f

        val outline = Path().apply {
            moveTo(tipX, h / 2f)
            lineTo(bodyLeft, h * 0.16f)
            lineTo(w * 0.94f, h * 0.16f)
            lineTo(w * 0.94f, h * 0.84f)
            lineTo(bodyLeft, h * 0.84f)
            close()
        }
        drawPath(
            path = outline,
            color = color,
            style = Stroke(width = stroke, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )

        // The "x" inside.
        val left = w * 0.50f
        val right = w * 0.80f
        val top = h * 0.36f
        val bottom = h * 0.64f
        drawLine(color, Offset(left, top), Offset(right, bottom), stroke, StrokeCap.Round)
        drawLine(color, Offset(right, top), Offset(left, bottom), stroke, StrokeCap.Round)
    }
}

/** Submit: a right-pointing arrow. */
@Composable
fun SubmitGlyph(
    modifier: Modifier = Modifier,
    color: Color = Ink
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.09f

        drawLine(color, Offset(w * 0.14f, h / 2f), Offset(w * 0.82f, h / 2f), stroke, StrokeCap.Round)

        val head = Path().apply {
            moveTo(w * 0.56f, h * 0.24f)
            lineTo(w * 0.86f, h * 0.50f)
            lineTo(w * 0.56f, h * 0.76f)
        }
        drawPath(
            path = head,
            color = color,
            style = Stroke(width = stroke, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
    }
}
