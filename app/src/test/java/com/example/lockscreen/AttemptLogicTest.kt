package com.example.lockscreen

import com.example.lockscreen.domain.AttemptOutcome
import com.example.lockscreen.domain.InMemoryAttemptHistory
import com.example.lockscreen.domain.RegisterAttemptUseCase
import com.example.lockscreen.domain.UnlockMode
import com.example.lockscreen.ui.lockscreen.LockScreenViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Fast JVM tests (no emulator needed) that pin down the one rule this app has.
 * Run them with:  ./gradlew :app:testDebugUnitTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttemptLogicTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registerAttempt = RegisterAttemptUseCase()

    // ------------------------------------------------------------- the rule

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

    // --------------------------------------------------------- PIN entry

    @Test
    fun `a 4-digit PIN submits itself and clears`() = runTest {
        val viewModel = LockScreenViewModel(InMemoryAttemptHistory(UnlockMode.PIN_4))

        "917".forEach(viewModel::onDigitEntered)
        advanceUntilIdle()
        // Three digits is not a complete entry, so nothing has happened yet.
        assertEquals("917", viewModel.uiState.value.pin)
        assertEquals(0, viewModel.uiState.value.attemptCount)

        viewModel.onDigitEntered('4')
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.pin)
        assertEquals(1, viewModel.uiState.value.attemptCount)
        assertFalse(viewModel.uiState.value.isUnlocking)
    }

    @Test
    fun `a 6-digit PIN needs six digits before it submits`() = runTest {
        val viewModel = LockScreenViewModel(InMemoryAttemptHistory(UnlockMode.PIN_6))

        "12345".forEach(viewModel::onDigitEntered)
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.attemptCount)

        viewModel.onDigitEntered('6')
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.attemptCount)
    }

    @Test
    fun `four entries unlock regardless of the digits - nothing is validated`() = runTest {
        val history = InMemoryAttemptHistory(UnlockMode.PIN_4)
        val viewModel = LockScreenViewModel(history)

        // Four completely different numbers; only the count matters.
        listOf("1111", "2580", "9764", "0000").forEach { entry ->
            entry.forEach(viewModel::onDigitEntered)
            advanceUntilIdle()
        }

        assertEquals(4, viewModel.uiState.value.attemptCount)
        assertTrue(viewModel.uiState.value.isUnlocking)
    }

    @Test
    fun `backspace removes the last digit only`() = runTest {
        val viewModel = LockScreenViewModel(InMemoryAttemptHistory(UnlockMode.PIN_4))

        "42".forEach(viewModel::onDigitEntered)
        viewModel.onBackspace()

        assertEquals("4", viewModel.uiState.value.pin)
    }

    // ----------------------------------------------------------- patterns

    @Test
    fun `a pattern is recorded with one-based dot numbers`() = runTest {
        val history = InMemoryAttemptHistory(UnlockMode.PATTERN)
        val viewModel = LockScreenViewModel(history)

        viewModel.onPatternChanged(listOf(0, 1, 2, 5))
        viewModel.onPatternCompleted()

        assertEquals(listOf("1-2-3-6"), history.recent())
        assertEquals(1, viewModel.uiState.value.attemptCount)
    }

    @Test
    fun `a pattern that is too short is discarded, not counted`() = runTest {
        val viewModel = LockScreenViewModel(InMemoryAttemptHistory(UnlockMode.PATTERN))

        viewModel.onPatternChanged(listOf(4))
        viewModel.onPatternCompleted()

        assertEquals(0, viewModel.uiState.value.attemptCount)
        assertTrue(viewModel.uiState.value.pattern.isEmpty())
    }

    // ------------------------------------------------- history and reveal

    @Test
    fun `only the last two entries are kept, newest first`() = runTest {
        val history = InMemoryAttemptHistory(UnlockMode.PIN_4)
        val viewModel = LockScreenViewModel(history)

        listOf("1111", "2222", "3333").forEach { entry ->
            entry.forEach(viewModel::onDigitEntered)
            advanceUntilIdle()
        }

        assertEquals(listOf("3333", "2222"), history.recent())
    }

    @Test
    fun `the reveal shows recent entries without counting as an attempt`() = runTest {
        val viewModel = LockScreenViewModel(InMemoryAttemptHistory(UnlockMode.PIN_4))

        "7788".forEach(viewModel::onDigitEntered)
        advanceUntilIdle()

        viewModel.onEmergencyLongPressed()

        assertEquals(listOf("7788"), viewModel.uiState.value.revealedEntries)
        assertEquals(1, viewModel.uiState.value.attemptCount)

        viewModel.onRevealDismissed()
        assertEquals(null, viewModel.uiState.value.revealedEntries)
    }

    @Test
    fun `clearing the history leaves nothing to reveal`() = runTest {
        val history = InMemoryAttemptHistory(UnlockMode.PIN_4)
        val viewModel = LockScreenViewModel(history)

        "4242".forEach(viewModel::onDigitEntered)
        advanceUntilIdle()

        viewModel.onClearHistory()
        viewModel.onEmergencyLongPressed()

        assertTrue(viewModel.uiState.value.revealedEntries!!.isEmpty())
    }

    // ------------------------------------------------------- mode switching

    @Test
    fun `switching mode clears the entry but keeps the attempt count`() = runTest {
        val history = InMemoryAttemptHistory(UnlockMode.PIN_4)
        val viewModel = LockScreenViewModel(history)

        "1234".forEach(viewModel::onDigitEntered)
        advanceUntilIdle()
        "56".forEach(viewModel::onDigitEntered)

        viewModel.onModeSelected(UnlockMode.PATTERN)

        assertEquals("", viewModel.uiState.value.pin)
        assertEquals(UnlockMode.PATTERN, viewModel.uiState.value.mode)
        assertEquals(1, viewModel.uiState.value.attemptCount)
        // The choice is persisted so the next launch starts in the same mode.
        assertEquals(UnlockMode.PATTERN, history.mode)
    }
}
