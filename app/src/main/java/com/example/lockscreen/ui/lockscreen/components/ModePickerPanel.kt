package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lockscreen.R
import com.example.lockscreen.domain.UnlockMode
import com.example.lockscreen.ui.theme.BloomCyan
import com.example.lockscreen.ui.theme.GlassStroke
import com.example.lockscreen.ui.theme.Ink
import com.example.lockscreen.ui.theme.InkFaint
import com.example.lockscreen.ui.theme.InkMuted

/**
 * The hidden settings panel, opened from the invisible strip in the top-left
 * corner. Lets the performer pick the unlock style before handing the phone
 * over, and wipe anything that was recorded.
 *
 * It is deliberately plain and quick to dismiss: a tap anywhere outside closes it.
 */
@Composable
fun ModePickerPanel(
    selected: UnlockMode,
    onModeSelected: (UnlockMode) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
            // Tapping the scrim dismisses. No ripple, so nothing flashes.
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xF21A1430))
                .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
                // Swallow taps on the card itself so it does not dismiss.
                .clickable(interactionSource = null, indication = null, onClick = {})
                .padding(vertical = 18.dp)
        ) {
            Text(
                text = stringResource(R.string.picker_title),
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint,
                modifier = Modifier.padding(start = 22.dp, bottom = 10.dp)
            )

            UnlockMode.entries.forEach { mode ->
                ModeRow(
                    label = stringResource(mode.labelRes),
                    isSelected = mode == selected,
                    onClick = { onModeSelected(mode) }
                )
            }

            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 22.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GlassStroke)
            )

            Text(
                text = stringResource(R.string.picker_clear),
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClearHistory)
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ModeRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Ink else InkMuted
        )
        // A filled dot marks the active style.
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (isSelected) BloomCyan else Color.Transparent)
                .border(1.dp, if (isSelected) BloomCyan else GlassStroke, CircleShape)
        )
    }
}

/** Human-readable name for each unlock style. */
private val UnlockMode.labelRes: Int
    get() = when (this) {
        UnlockMode.PIN_4 -> R.string.mode_pin_4
        UnlockMode.PIN_6 -> R.string.mode_pin_6
        UnlockMode.PATTERN -> R.string.mode_pattern
    }
