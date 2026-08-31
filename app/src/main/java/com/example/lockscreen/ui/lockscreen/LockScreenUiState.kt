package com.example.lockscreen.ui.lockscreen

import com.example.lockscreen.domain.UnlockMode

/**
 * Everything the lock screen needs to draw itself, held in one immutable object.
 *
 * @param mode which unlock style is active.
 * @param pin digits typed so far in a PIN mode. Shown as dots, never validated.
 * @param pattern dot indices (0..8) touched so far in pattern mode.
 * @param attemptCount how many entries have been submitted.
 * @param isUnlocking true once the final attempt has landed; the UI plays the
 *        unlock animation and blocks further input while this is set.
 * @param isModePickerVisible true while the hidden settings panel is open.
 * @param revealedEntries non-null while the reveal card is showing; holds the
 *        most recent entries, newest first.
 */
data class LockScreenUiState(
    val mode: UnlockMode = UnlockMode.PIN_4,
    val pin: String = "",
    val pattern: List<Int> = emptyList(),
    val attemptCount: Int = 0,
    val isUnlocking: Boolean = false,
    val isModePickerVisible: Boolean = false,
    val revealedEntries: List<String>? = null
) {
    /** True while any overlay is up, or the app is on its way out. */
    val isInputBlocked: Boolean
        get() = isUnlocking || isModePickerVisible || revealedEntries != null
}

/**
 * One-shot events. These are *not* state: replaying them after a rotation would
 * re-trigger an animation, so they travel through a Channel instead.
 */
sealed interface LockScreenEffect {

    /** Attempts 1-3: a short horizontal shake, then the field is already cleared. */
    data object Shake : LockScreenEffect

    /** Attempt 4: play the unlock animation, then finish the Activity. */
    data object Unlock : LockScreenEffect
}
