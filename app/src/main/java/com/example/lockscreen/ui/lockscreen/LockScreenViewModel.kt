package com.example.lockscreen.ui.lockscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lockscreen.domain.AttemptHistory
import com.example.lockscreen.domain.AttemptOutcome
import com.example.lockscreen.domain.MIN_PATTERN_DOTS
import com.example.lockscreen.domain.RegisterAttemptUseCase
import com.example.lockscreen.domain.UnlockMode
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds the attempt counter, the entry in progress and the chosen unlock style.
 *
 * Living in a ViewModel means all of that survives configuration changes such as
 * rotation - turning the phone does not grant a free extra attempt.
 */
class LockScreenViewModel(
    private val history: AttemptHistory,
    private val registerAttempt: RegisterAttemptUseCase = RegisterAttemptUseCase()
) : ViewModel() {

    // The mode the user picked last time is restored on launch.
    private val _uiState = MutableStateFlow(LockScreenUiState(mode = history.mode))
    val uiState = _uiState.asStateFlow()

    // Channel (not StateFlow) so each effect is delivered exactly once.
    private val _effects = Channel<LockScreenEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // ---------------------------------------------------------------- PIN input

    /**
     * Appends one digit. When the digit completes the PIN the entry submits
     * itself after a beat, which is how a real fixed-length PIN screen behaves
     * and why there is no confirm button.
     */
    fun onDigitEntered(digit: Char) {
        val state = _uiState.value
        if (state.isInputBlocked || !state.mode.isPin) return
        if (state.pin.length >= state.mode.pinLength) return

        val updated = state.pin + digit
        _uiState.update { it.copy(pin = updated) }

        if (updated.length == state.mode.pinLength) {
            viewModelScope.launch {
                // Let the final dot finish filling before the screen reacts.
                delay(AUTO_SUBMIT_DELAY_MS)
                // Guard against the user deleting a digit during that pause.
                if (_uiState.value.pin == updated) submit(updated)
            }
        }
    }

    /** Removes the last digit. Does nothing when the field is already empty. */
    fun onBackspace() {
        _uiState.update { state ->
            if (state.isInputBlocked || state.pin.isEmpty()) state
            else state.copy(pin = state.pin.dropLast(1))
        }
    }

    // ------------------------------------------------------------ Pattern input

    /** Called continuously while the finger is down, with the dots touched so far. */
    fun onPatternChanged(dots: List<Int>) {
        if (_uiState.value.isInputBlocked) return
        _uiState.update { it.copy(pattern = dots) }
    }

    /** Called when the finger lifts. A pattern that is too short is just cleared. */
    fun onPatternCompleted() {
        val state = _uiState.value
        if (state.isInputBlocked) return

        if (state.pattern.size < MIN_PATTERN_DOTS) {
            _uiState.update { it.copy(pattern = emptyList()) }
            return
        }
        // Dots are stored 0-based but read back 1-based, like a phone keypad.
        submit(state.pattern.joinToString("-") { (it + 1).toString() })
    }

    // ----------------------------------------------------------- The whole rule

    /**
     * The entire behaviour of the app in one function.
     *
     * [entry] is recorded so the hidden reveal can show it later, but it is
     * never compared against anything. Only the attempt count decides what
     * happens next.
     */
    private fun submit(entry: String) {
        val state = _uiState.value
        if (state.isUnlocking) return

        history.record(entry)

        when (val outcome = registerAttempt(state.attemptCount)) {
            // Attempts 1-3: clear the entry, stay put, no error message at all.
            is AttemptOutcome.Remain -> {
                _uiState.update {
                    it.copy(pin = "", pattern = emptyList(), attemptCount = outcome.attemptNumber)
                }
                _effects.trySend(LockScreenEffect.Shake)
            }

            // Attempt 4: freeze input and ask the screen to run the exit animation.
            is AttemptOutcome.Exit -> {
                _uiState.update {
                    it.copy(
                        pin = "",
                        pattern = emptyList(),
                        attemptCount = outcome.attemptNumber,
                        isUnlocking = true
                    )
                }
                _effects.trySend(LockScreenEffect.Unlock)
            }
        }
    }

    // --------------------------------------------------- Hidden settings panel

    /** Opened by tapping the invisible strip in the top-left corner. */
    fun onHiddenZoneTapped() {
        _uiState.update { it.copy(isModePickerVisible = true) }
    }

    fun onModePickerDismissed() {
        _uiState.update { it.copy(isModePickerVisible = false) }
    }

    /** Switching style clears the entry in progress but keeps the attempt count. */
    fun onModeSelected(mode: UnlockMode) {
        history.mode = mode
        _uiState.update {
            it.copy(
                mode = mode,
                pin = "",
                pattern = emptyList(),
                isModePickerVisible = false
            )
        }
    }

    /** Wipes the stored entries so nothing can be revealed afterwards. */
    fun onClearHistory() {
        history.clear()
        _uiState.update { it.copy(isModePickerVisible = false) }
    }

    // ----------------------------------------------------------- Hidden reveal

    /**
     * Long-pressing "Emergency call" shows the recent entries. This deliberately
     * does not count as an attempt and does not touch the entry in progress.
     */
    fun onEmergencyLongPressed() {
        _uiState.update { it.copy(revealedEntries = history.recent()) }
    }

    fun onRevealDismissed() {
        _uiState.update { it.copy(revealedEntries = null) }
    }

    private companion object {
        /** Pause between the last digit landing and the entry submitting. */
        const val AUTO_SUBMIT_DELAY_MS = 160L
    }
}
