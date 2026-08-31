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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockscreen.R
import com.example.lockscreen.ui.theme.GlassStroke
import com.example.lockscreen.ui.theme.Ink
import com.example.lockscreen.ui.theme.InkFaint
import com.example.lockscreen.ui.theme.InkMuted

/**
 * The hidden reveal: shows the entries that were submitted most recently,
 * newest first.
 *
 * Reached by long-pressing "Emergency call". A plain tap on that label does
 * nothing, so the reveal cannot be opened by accident. Tapping anywhere closes
 * this card again.
 */
@Composable
fun RevealCard(
    entries: List<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(interactionSource = null, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xF21A1430))
                .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.reveal_title),
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )

            if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.reveal_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkMuted
                )
            } else {
                entries.forEachIndexed { index, entry ->
                    EntryRow(
                        // 0 is the newest, so it is the "last" attempt.
                        label = stringResource(
                            if (index == 0) R.string.reveal_last else R.string.reveal_previous
                        ),
                        entry = entry
                    )
                }
            }

            Text(
                text = stringResource(R.string.reveal_dismiss_hint),
                style = MaterialTheme.typography.labelSmall,
                color = InkFaint
            )
        }
    }
}

@Composable
private fun EntryRow(label: String, entry: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = InkMuted
        )
        Text(
            text = entry,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            ),
            color = Ink
        )
    }
}
