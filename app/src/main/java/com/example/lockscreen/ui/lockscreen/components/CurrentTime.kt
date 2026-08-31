package com.example.lockscreen.ui.lockscreen.components

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The clock text plus the date line shown beneath it. */
data class ClockText(
    val time: String,
    val date: String,
    val amPm: String
)

/**
 * Emits a fresh [Date] once per second.
 *
 * The delay is calculated so the tick lands on the whole second instead of
 * slowly sliding out of sync.
 */
@Composable
private fun rememberNow(): State<Date> {
    val now = remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now.value = Date()
            delay(1_000L - (System.currentTimeMillis() % 1_000L))
        }
    }
    return now
}

/**
 * Formats the current time for display, honouring the device's 12/24-hour
 * setting and the user's locale.
 *
 * `SimpleDateFormat` is used (rather than `java.time`) so the app runs on
 * API 24 without needing library desugaring.
 */
@Composable
fun rememberClockText(): ClockText {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale: Locale = primaryLocaleOf(configuration)
    val is24Hour = DateFormat.is24HourFormat(context)

    val timeFormat = remember(is24Hour, locale) {
        SimpleDateFormat(if (is24Hour) "HH:mm" else "h:mm", locale)
    }
    val amPmFormat = remember(locale) { SimpleDateFormat("a", locale) }
    val dateFormat = remember(locale) { SimpleDateFormat("EEEE, d MMMM", locale) }

    val now by rememberNow()

    return ClockText(
        time = timeFormat.format(now),
        date = dateFormat.format(now),
        amPm = if (is24Hour) "" else amPmFormat.format(now)
    )
}

/** Small helper: reads the primary locale in a way that works on every API level. */
@Suppress("DEPRECATION")
private fun primaryLocaleOf(configuration: android.content.res.Configuration): Locale =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        configuration.locales[0]
    } else {
        configuration.locale
    }

/** The short "HH:mm" clock reused by the fake status bar. */
@Composable
fun rememberStatusBarTime(): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = primaryLocaleOf(configuration)
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val format = remember(pattern, locale) { SimpleDateFormat(pattern, locale) }
    val now by rememberNow()
    return format.format(now)
}
