package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lockscreen.R
import com.example.lockscreen.ui.theme.GlassIdle
import com.example.lockscreen.ui.theme.Ink

/**
 * The "Emergency call" affordance at the bottom of the screen.
 *
 * A tap does nothing at all, exactly as a decoy should. A long press is the
 * hidden gesture that opens the reveal.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmergencyCall(
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(GlassIdle)
            .combinedClickable(
                // Deliberately inert: the label is only ever a decoy.
                onClick = {},
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            )
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PhoneGlyph(modifier = Modifier.size(14.dp), color = Ink)
        Text(
            text = stringResource(R.string.emergency_call),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink
        )
    }
}
