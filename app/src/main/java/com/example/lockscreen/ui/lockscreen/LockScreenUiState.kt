package com.example.lockscreen.ui.lockscreen

/**
 * Everything the lock screen needs to draw itself, held in one immutable object.
 *
 * @param pin the digits typed so far. Shown as dots only – never validated.
 * @param attemptCount how many times the user has pressed submit.
 * @param isUnlocking true once the final attempt has been made; the UI plays the
 *        unlock animation and blocks further input while this is set.
 */
data class LockScreenUiState(
    val pin: String = "",
    val attemptCount: Int = 0,
    val isUnlocking: Boolean = false
) {
    /** The submit button is only tappable when at least one digit was entered. */
    val canSubmit: Boolean get() = pin.isNotEmpty() && !isUnlocking
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
