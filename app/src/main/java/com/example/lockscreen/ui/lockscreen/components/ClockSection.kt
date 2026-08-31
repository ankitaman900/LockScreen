package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lockscreen.ui.theme.Ink
import com.example.lockscreen.ui.theme.InkMuted

/**
 * The large clock, the AM/PM marker (only when the device uses 12-hour time),
 * the date and the small padlock underneath – the classic lock-screen stack.
 */
@Composable
fun ClockSection(modifier: Modifier = Modifier) {
    val clock = rememberClockText()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = clock.time,
                style = MaterialTheme.typography.displayLarge,
                color = Ink
            )
            if (clock.amPm.isNotEmpty()) {
                Text(
                    text = clock.amPm,
                    style = MaterialTheme.typography.titleMedium,
                    color = InkMuted,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
            }
        }

        Text(
            text = clock.date,
            style = MaterialTheme.typography.titleMedium,
            color = InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        LockGlyph(
            modifier = Modifier
                .padding(top = 22.dp)
                .size(22.dp),
            color = InkMuted
        )
    }
}
