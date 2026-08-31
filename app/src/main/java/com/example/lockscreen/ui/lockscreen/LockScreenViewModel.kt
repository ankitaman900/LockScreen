package com.example.lockscreen.ui.lockscreen

import androidx.lifecycle.ViewModel
import com.example.lockscreen.domain.AttemptOutcome
import com.example.lockscreen.domain.RegisterAttemptUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the attempt counter and the digits typed so far.
 *
 * Living in a ViewModel means the count survives configuration changes such as
 * rotation – rotating the phone does not give the user a free extra attempt.
 */
class LockScreenViewModel @JvmOverloads constructor(
    // @JvmOverloads generates the no-argument constructor that the default
    // ViewModelProvider factory needs, while tests can still inject a fake.
    private val registerAttempt: RegisterAttemptUseCase = RegisterAttemptUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockScreenUiState())
    val uiState = _uiState.asStateFlow()

    // Channel (not StateFlow) so each effect is delivered exactly once.
    private val _effects = Channel<LockScreenEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** Appends a digit, up to [MAX_PIN_LENGTH]. Extra taps are ignored. */
    fun onDigitEntered(digit: Char) {
        _uiState.update { state ->
            if (state.isUnlocking || state.pin.length >= MAX_PIN_LENGTH) state
            else state.copy(pin = state.pin + digit)
        }
    }

    /** Removes the last digit. Does nothing when the field is already empty. */
    fun onBackspace() {
        _uiState.update { state ->
            if (state.isUnlocking || state.pin.isEmpty()) state
            else state.copy(pin = state.pin.dropLast(1))
        }
    }

    /**
     * The whole behaviour of the app in one function.
     *
     * The value of [LockScreenUiState.pin] is never inspected – we only count.
     */
    fun onSubmit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        when (val outcome = registerAttempt(state.attemptCount)) {
            // Attempts 1-3: clear the input, stay put, no error message at all.
            is AttemptOutcome.Remain -> {
                _uiState.update {
                    it.copy(pin = "", attemptCount = outcome.attemptNumber)
                }
                _effects.trySend(LockScreenEffect.Shake)
            }

            // Attempt 4: lock the UI and ask the screen to run the exit animation.
            is AttemptOutcome.Exit -> {
                _uiState.update {
                    it.copy(pin = "", attemptCount = outcome.attemptNumber, isUnlocking = true)
                }
                _effects.trySend(LockScreenEffect.Unlock)
            }
        }
    }

    private companion object {
        const val MAX_PIN_LENGTH = 8
    }
}
