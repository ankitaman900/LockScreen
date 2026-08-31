package com.example.lockscreen.domain

/**
 * The three unlock styles the hidden settings panel can switch between.
 *
 * Changing the mode only changes how the entry is *collected*. What happens
 * afterwards is identical in every mode: the entry is stored, the attempt is
 * counted, and nothing is ever validated.
 */
enum class UnlockMode {
    PIN_4,
    PIN_6,
    PATTERN;

    /**
     * How many digits complete an entry. A fixed-length PIN submits itself the
     * moment the last digit lands, exactly like a real lock screen, which is
     * why there is no confirm button anywhere in the UI.
     *
     * Zero for [PATTERN], where the entry ends when the finger lifts instead.
     */
    val pinLength: Int
        get() = when (this) {
            PIN_4 -> 4
            PIN_6 -> 6
            PATTERN -> 0
        }

    val isPin: Boolean get() = this != PATTERN
}

/** A pattern shorter than this is treated as a slip and simply cleared. */
const val MIN_PATTERN_DOTS = 2
