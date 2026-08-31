package com.example.lockscreen.domain

/**
 * DOMAIN LAYER
 *
 * Pure Kotlin, no Android imports and no Compose imports. This is the single
 * place that decides what happens when the user submits a number, which makes
 * the rule trivial to read and to unit-test.
 *
 * The rule is deliberately simple: the entered number is NEVER looked at.
 * Only the number of attempts matters.
 */

/** How many submissions it takes before the app closes itself. */
const val ATTEMPTS_BEFORE_EXIT = 4

/** The two possible results of a submission. */
sealed interface AttemptOutcome {

    /** Attempts 1..3 – clear the field and stay on the lock screen. */
    data class Remain(val attemptNumber: Int) : AttemptOutcome

    /** Attempt 4 – play the unlock animation and leave the app. */
    data class Exit(val attemptNumber: Int) : AttemptOutcome
}

/**
 * Counts one submission and reports what should happen next.
 *
 * Note that the PIN itself is not a parameter: there is nothing to validate.
 *
 * @param attemptsBeforeExit injectable so tests can use a smaller number.
 */
class RegisterAttemptUseCase(
    private val attemptsBeforeExit: Int = ATTEMPTS_BEFORE_EXIT
) {
    /**
     * @param previousAttempts how many submissions happened before this one.
     * @return [AttemptOutcome.Remain] while below the threshold, otherwise
     *         [AttemptOutcome.Exit].
     */
    operator fun invoke(previousAttempts: Int): AttemptOutcome {
        val attemptNumber = previousAttempts + 1
        return if (attemptNumber < attemptsBeforeExit) {
            AttemptOutcome.Remain(attemptNumber)
        } else {
            AttemptOutcome.Exit(attemptNumber)
        }
    }
}
