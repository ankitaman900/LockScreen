package com.example.lockscreen.domain

/**
 * Remembers the last couple of entries so the hidden reveal can show them.
 *
 * It is an interface for two reasons: the unit tests use a plain in-memory
 * implementation and need no Android runtime, and the real app can persist to
 * disk without the ViewModel knowing or caring how.
 */
interface AttemptHistory {

    /** The unlock style the user last chose. Survives restarts. */
    var mode: UnlockMode

    /** Stores one entry, pushing out anything older than the last [MAX_ENTRIES]. */
    fun record(entry: String)

    /** The most recent entries, newest first. At most [MAX_ENTRIES] of them. */
    fun recent(): List<String>

    /** Forgets everything that was recorded. */
    fun clear()

    companion object {
        /** The reveal shows the last two attempts, so that is all we keep. */
        const val MAX_ENTRIES = 2
    }
}

/** Non-persistent implementation used by unit tests and Compose previews. */
class InMemoryAttemptHistory(
    override var mode: UnlockMode = UnlockMode.PIN_4
) : AttemptHistory {

    private val entries = ArrayDeque<String>()

    override fun record(entry: String) {
        entries.addFirst(entry)
        while (entries.size > AttemptHistory.MAX_ENTRIES) {
            entries.removeLast()
        }
    }

    override fun recent(): List<String> = entries.toList()

    override fun clear() {
        entries.clear()
    }
}
