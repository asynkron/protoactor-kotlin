package proto.actor

import java.lang.System.currentTimeMillis
import java.time.Duration

open class RestartStatistics(var failureCount: Int,var lastFailureTimeMillis: Long) {
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

