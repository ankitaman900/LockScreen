package com.example.lockscreen

import com.example.lockscreen.domain.AttemptOutcome
import com.example.lockscreen.domain.RegisterAttemptUseCase
import com.example.lockscreen.ui.lockscreen.LockScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fast JVM tests (no emulator needed) that pin down the one rule this app has.
 * Run them with:  ./gradlew :app:testDebugUnitTest
 */
class AttemptLogicTest {

    private val registerAttempt = RegisterAttemptUseCase()

    @Test
    fun `first three attempts keep the user on the lock screen`() {
        assertTrue(registerAttempt(0) is AttemptOutcome.Remain)
        assertTrue(registerAttempt(1) is AttemptOutcome.Remain)
        assertTrue(registerAttempt(2) is AttemptOutcome.Remain)
    }

    @Test
    fun `fourth attempt exits the app`() {
        val outcome = registerAttempt(3)
        assertTrue(outcome is AttemptOutcome.Exit)
        assertEquals(4, (outcome as AttemptOutcome.Exit).attemptNumber)
    }

    @Test
    fun `submitting clears the pin and counts up without unlocking`() {
        val viewModel = LockScreenViewModel()

        "917".forEach(viewModel::onDigitEntered)
        assertEquals("917", viewModel.uiState.value.pin)

        viewModel.onSubmit()

        assertEquals("", viewModel.uiState.value.pin)
        assertEquals(1, viewModel.uiState.value.attemptCount)
        assertFalse(viewModel.uiState.value.isUnlocking)
    }

    @Test
    fun `any digits unlock on the fourth submit - the value is never checked`() {
        val viewModel = LockScreenViewModel()

        // Four completely different numbers; only the count matters.
        listOf("1", "2222", "77", "0").forEach { number ->
            number.forEach(viewModel::onDigitEntered)
            viewModel.onSubmit()
        }

        assertEquals(4, viewModel.uiState.value.attemptCount)
        assertTrue(viewModel.uiState.value.isUnlocking)
    }

    @Test
    fun `submitting an empty pin is ignored`() {
        val viewModel = LockScreenViewModel()

        viewModel.onSubmit()

        assertEquals(0, viewModel.uiState.value.attemptCount)
    }

    @Test
    fun `backspace removes the last digit only`() {
        val viewModel = LockScreenViewModel()

        "42".forEach(viewModel::onDigitEntered)
        viewModel.onBackspace()

        assertEquals("4", viewModel.uiState.value.pin)
    }
}
