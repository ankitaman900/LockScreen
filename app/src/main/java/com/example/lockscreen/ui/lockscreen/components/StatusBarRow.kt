package com.example.lockscreen.ui.lockscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lockscreen.ui.theme.Ink

/**
 * A minimal, decorative status bar drawn by the app itself.
 *
 * The real system status bar is hidden (see MainActivity) so the screen feels
 * immersive; this row simply mimics the familiar layout. The battery level is a
 * fixed decorative value – reading the real one would add nothing to the demo.
 */
@Composable
fun StatusBarRow(modifier: Modifier = Modifier) {
    val time = rememberStatusBarTime()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = Ink
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            SignalGlyph(modifier = Modifier.size(width = 15.dp, height = 10.dp), color = Ink)
            WifiGlyph(modifier = Modifier.size(width = 15.dp, height = 11.dp), color = Ink)
            BatteryGlyph(modifier = Modifier.size(width = 22.dp, height = 11.dp), color = Ink, level = 0.82f)
        }
    }
}
