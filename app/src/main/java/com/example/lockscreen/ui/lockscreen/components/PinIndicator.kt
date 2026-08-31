package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.lockscreen.ui.theme.Ink
import com.example.lockscreen.ui.theme.InkFaint

/**
 * The row of PIN dots.
 *
 * [capacity] hollow dots are always shown, and the first [filled] of them are
 * solid. Seeing exactly how many digits are expected is what makes a
 * fixed-length PIN screen feel real, and it tells the user when the entry is
 * about to submit itself.
 *
 * Note that only the *length* of the PIN reaches this composable, never the
 * digits themselves.
 */
@Composable
fun PinIndicator(
    filled: Int,
    capacity: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            // A fixed height keeps the layout from jumping when the dots clear.
            .height(24.dp)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(capacity) { index ->
            PinDot(isFilled = index < filled)
        }
    }
}

@Composable
private fun PinDot(isFilled: Boolean) {
    // A filled dot pops slightly as it lands, then settles.
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1f else 0.72f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dotScale"
    )
    val fill by animateColorAsState(
        targetValue = if (isFilled) Ink else Color.Transparent,
        label = "dotFill"
    )

    Box(
        modifier = Modifier
            .size(13.dp)
            .scale(scale)
            .background(color = fill, shape = CircleShape)
            .border(width = 1.5.dp, color = if (isFilled) Color.Transparent else InkFaint, shape = CircleShape)
    )
}
