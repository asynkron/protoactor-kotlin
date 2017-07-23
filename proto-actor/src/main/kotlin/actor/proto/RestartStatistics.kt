package actor.proto

import java.lang.System.currentTimeMillis
import java.time.Duration

class RestartStatistics(var failureCount: Int, private var lastFailureTimeMillis: Long) {
    fun fail() {
        failureCount++
        lastFailureTimeMillis = now()
    }

    fun reset() {
        failureCount = 0
    }

    fun restart() {
        lastFailureTimeMillis = now()
    }

    private fun now(): Long = currentTimeMillis()

    fun isWithinDuration(within: Duration): Boolean = (now() - lastFailureTimeMillis) < within.toMillis()
}

