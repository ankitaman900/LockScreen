package com.example.lockscreen.data

import android.content.Context
import androidx.core.content.edit
import com.example.lockscreen.domain.AttemptHistory
import com.example.lockscreen.domain.UnlockMode

/**
 * Persists the recent entries and the chosen unlock style in the app's own
 * private SharedPreferences file.
 *
 * Persistence is what makes the reveal actually useful: the app closes itself on
 * the fourth attempt, so without it the entries would be gone before they could
 * ever be read back.
 *
 * The file lives in the app's private storage, which other apps cannot read on a
 * normal (non-rooted) device. Nothing is ever sent anywhere - the app has no
 * network permission at all. "Clear saved entries" in the hidden panel wipes it.
 */
class PrefsAttemptHistory(context: Context) : AttemptHistory {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override var mode: UnlockMode
        get() {
            val stored = prefs.getString(KEY_MODE, null) ?: return UnlockMode.PIN_4
            // valueOf throws if the stored name is unknown, e.g. after a downgrade.
            return runCatching { UnlockMode.valueOf(stored) }.getOrDefault(UnlockMode.PIN_4)
        }
        set(value) = prefs.edit { putString(KEY_MODE, value.name) }

    override fun record(entry: String) {
        val updated = (listOf(entry) + recent()).take(AttemptHistory.MAX_ENTRIES)
        prefs.edit { putString(KEY_ENTRIES, updated.joinToString(SEPARATOR)) }
    }

    override fun recent(): List<String> =
        prefs.getString(KEY_ENTRIES, null)
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    override fun clear() = prefs.edit { remove(KEY_ENTRIES) }

    private companion object {
        const val FILE_NAME = "lockscreen_history"
        const val KEY_ENTRIES = "recent_entries"
        const val KEY_MODE = "unlock_mode"

        // ASCII "unit separator": it can never appear in a PIN or a pattern
        // label, so joining and splitting is always unambiguous.
        const val SEPARATOR = "\u001F"
    }
}
