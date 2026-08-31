package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.lockscreen.ui.theme.BloomCyan
import com.example.lockscreen.ui.theme.Ink
import com.example.lockscreen.ui.theme.InkFaint

/** Dots per side of the grid. */
private const val GRID = 3
private const val DOT_COUNT = GRID * GRID

/**
 * A 3x3 unlock pattern.
 *
 * Behaves like the real thing: dragging over a dot selects it, dragging *across*
 * an unselected dot on the way between two others picks that one up too, and
 * lifting the finger ends the entry.
 *
 * The composable is stateless as far as the selection goes - it reports every
 * change through [onChanged] and the completed gesture through [onCompleted].
 *
 * @param selected dot indices chosen so far, 0..8 in reading order.
 */
@Composable
fun PatternGrid(
    selected: List<Int>,
    enabled: Boolean,
    onChanged: (List<Int>) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    // pointerInput is keyed on `enabled` only, so its lambda would otherwise
    // capture whatever `selected` happened to be when the gesture handler was
    // installed. rememberUpdatedState keeps these readable and current.
    val currentSelection by rememberUpdatedState(selected)
    val notifyChanged by rememberUpdatedState(onChanged)
    val notifyCompleted by rememberUpdatedState(onCompleted)

    // Where the finger is right now, so a line can trail from the last dot to it.
    var fingerPosition by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier.pointerInput(enabled) {
            if (!enabled) return@pointerInput

            // The grid is laid out inside a square, inset far enough that the
            // outer dots' touch targets stay on screen.
            val padding = size.width.coerceAtMost(size.height) * DOT_INSET
            val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

            /** The dot under [position], or null if the finger is between dots. */
            fun dotAt(position: Offset): Int? {
                val step = (canvasSize.minDimension - 2 * padding) / (GRID - 1)
                val hitRadius = step * HIT_RADIUS
                return (0 until DOT_COUNT).firstOrNull { index ->
                    (dotCenter(index, canvasSize, padding) - position).getDistance() <= hitRadius
                }
            }

            /** Adds [dot] plus any dot the finger skipped over on the way there. */
            fun extend(current: List<Int>, dot: Int): List<Int> {
                if (dot in current) return current
                val previous = current.lastOrNull() ?: return current + dot
                val skipped = dotBetween(previous, dot)
                return if (skipped != null && skipped !in current) {
                    current + skipped + dot
                } else {
                    current + dot
                }
            }

            detectDragGestures(
                onDragStart = { start ->
                    fingerPosition = start
                    val dot = dotAt(start)
                    // A new gesture always begins a fresh pattern.
                    notifyChanged(if (dot != null) listOf(dot) else emptyList())
                    if (dot != null) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onDrag = { change, _ ->
                    fingerPosition = change.position
                    val dot = dotAt(change.position) ?: return@detectDragGestures
                    val existing = currentSelection
                    val updated = extend(existing, dot)
                    if (updated.size != existing.size) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        notifyChanged(updated)
                    }
                },
                onDragEnd = {
                    fingerPosition = null
                    notifyCompleted()
                },
                onDragCancel = {
                    fingerPosition = null
                    notifyCompleted()
                }
            )
        }
    ) {
        val padding = size.minDimension * DOT_INSET
        val step = (size.minDimension - 2 * padding) / (GRID - 1)
        val dotRadius = step * 0.09f
        val lineWidth = step * 0.055f

        // 1. The connecting lines, drawn under the dots.
        selected.zipWithNext { from, to ->
            drawLine(
                color = BloomCyan.copy(alpha = 0.85f),
                start = dotCenter(from, size, padding),
                end = dotCenter(to, size, padding),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }

        // 2. The line trailing from the last dot to the finger.
        val trailingFrom = selected.lastOrNull()
        val finger = fingerPosition
        if (trailingFrom != null && finger != null) {
            drawLine(
                color = BloomCyan.copy(alpha = 0.45f),
                start = dotCenter(trailingFrom, size, padding),
                end = finger,
                strokeWidth = lineWidth,
                cap = StrokeCap.Round
            )
        }

        // 3. The dots themselves.
        repeat(DOT_COUNT) { index ->
            val center = dotCenter(index, size, padding)
            val isSelected = index in selected

            if (isSelected) {
                // A soft halo makes the selection obvious at a glance.
                drawCircle(
                    color = BloomCyan.copy(alpha = 0.18f),
                    radius = dotRadius * 3.1f,
                    center = center
                )
                drawCircle(color = Ink, radius = dotRadius * 1.5f, center = center)
            } else {
                drawCircle(
                    color = InkFaint,
                    radius = dotRadius * 2.1f,
                    center = center,
                    style = Stroke(width = lineWidth * 0.75f)
                )
                drawCircle(color = Color.White.copy(alpha = 0.30f), radius = dotRadius, center = center)
            }
        }
    }
}

/** Centre of dot [index] inside a square canvas of [size] with [padding] inset. */
private fun dotCenter(index: Int, size: Size, padding: Float): Offset {
    val step = (size.minDimension - 2 * padding) / (GRID - 1)
    // The grid is centred if the canvas is not perfectly square.
    val originX = padding + (size.width - size.minDimension) / 2f
    val originY = padding + (size.height - size.minDimension) / 2f
    return Offset(
        x = originX + (index % GRID) * step,
        y = originY + (index / GRID) * step
    )
}

/**
 * The dot that sits exactly halfway between [a] and [b], or null when there
 * isn't one (adjacent dots, or a knight's-move jump).
 *
 * This is what makes dragging 1 -> 3 also pick up 2, like the system pattern lock.
 */
private fun dotBetween(a: Int, b: Int): Int? {
    val rowA = a / GRID
    val colA = a % GRID
    val rowB = b / GRID
    val colB = b % GRID
    // A midpoint only exists when both coordinates are an even distance apart.
    if ((rowA + rowB) % 2 != 0 || (colA + colB) % 2 != 0) return null
    val middle = ((rowA + rowB) / 2) * GRID + (colA + colB) / 2
    return if (middle != a && middle != b) middle else null
}

/** Fraction of the square kept clear around the outside of the grid. */
private const val DOT_INSET = 0.14f

/** Touch radius around each dot, as a fraction of the spacing between dots. */
private const val HIT_RADIUS = 0.42f
