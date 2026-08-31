package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.example.lockscreen.ui.theme.Ink

/**
 * Shows one dot per entered digit.
 *
 * The digits themselves are never displayed and never leave the ViewModel –
 * only the *length* of the PIN reaches this composable, which is a nice
 * reminder that the value is irrelevant to the app's logic.
 */
@Composable
fun PinIndicator(
    pinLength: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            // A fixed height keeps the layout from jumping when the dots clear.
            .height(24.dp)
            .clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pinLength) { index ->
            // key() gives every dot its own state, so a newly added dot animates
            // in while the existing ones stay put.
            key(index) { PinDot() }
        }
    }
}

@Composable
private fun PinDot() {
    val scale = remember { Animatable(0.35f) }

    // Runs once, when this particular dot first appears.
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .scale(scale.value)
            .background(color = Ink, shape = CircleShape)
    )
}
