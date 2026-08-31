package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lockscreen.R
import com.example.lockscreen.ui.theme.GlassIdle
import com.example.lockscreen.ui.theme.GlassPressed
import com.example.lockscreen.ui.theme.GlassStroke
import com.example.lockscreen.ui.theme.Ink

/** The three digit rows; the fourth row is backspace / 0 / submit. */
private val DIGIT_ROWS = listOf("123", "456", "789")

/**
 * The numeric PIN pad.
 *
 * It is fully responsive: the circular buttons are sized from the width the
 * parent gives us and clamped to a comfortable range, so the same code looks
 * right on a small phone, a large phone and a tablet.
 */
@Composable
fun Keypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    submitEnabled: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val buttonSize: Dp = (maxWidth / 4.4f).coerceIn(56.dp, 80.dp)
        val rowSpacing: Dp = (buttonSize * 0.22f).coerceIn(10.dp, 20.dp)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DIGIT_ROWS.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { digit ->
                        DigitButton(
                            digit = digit,
                            size = buttonSize,
                            enabled = enabled,
                            onClick = { onDigit(digit) }
                        )
                    }
                }
            }

            // Last row: delete – 0 – submit.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KeypadButton(
                    size = buttonSize,
                    enabled = enabled,
                    filled = false,
                    contentDescription = stringResource(R.string.cd_backspace),
                    onClick = onBackspace
                ) {
                    BackspaceGlyph(
                        modifier = Modifier.size(buttonSize * 0.38f),
                        color = Ink
                    )
                }

                DigitButton(
                    digit = '0',
                    size = buttonSize,
                    enabled = enabled,
                    onClick = { onDigit('0') }
                )

                KeypadButton(
                    size = buttonSize,
                    enabled = enabled && submitEnabled,
                    filled = true,
                    contentDescription = stringResource(R.string.cd_submit),
                    onClick = onSubmit
                ) {
                    SubmitGlyph(
                        modifier = Modifier.size(buttonSize * 0.40f),
                        color = Ink
                    )
                }
            }
        }
    }
}

@Composable
private fun DigitButton(
    digit: Char,
    size: Dp,
    enabled: Boolean,
    onClick: () -> Unit
) {
    KeypadButton(
        size = size,
        enabled = enabled,
        filled = true,
        contentDescription = stringResource(R.string.cd_digit, digit.toString()),
        onClick = onClick
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )
    }
}

/**
 * One circular "frosted glass" key.
 *
 * Two things animate on press:
 *  - the button scales down slightly (a spring, so the release feels bouncy);
 *  - the translucent background brightens.
 * A light haptic tick fires on every tap, which is what makes a software keypad
 * feel physical.
 */
@Composable
private fun KeypadButton(
    size: Dp,
    enabled: Boolean,
    filled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "keyScale"
    )
    val background by animateColorAsState(
        targetValue = when {
            !filled -> if (isPressed) GlassPressed else Color.Transparent
            isPressed -> GlassPressed
            else -> GlassIdle
        },
        animationSpec = tween(durationMillis = 140),
        label = "keyBackground"
    )

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .alpha(if (enabled) 1f else 0.35f)
            .background(color = background, shape = CircleShape)
            .border(width = if (filled) 1.dp else 0.dp, color = if (filled) GlassStroke else Color.Transparent, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClickLabel = contentDescription,
                role = Role.Button
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}
