package proto.actor

import java.lang.System.currentTimeMillis
import java.time.Duration
import java.util.*

open class RestartStatistics(var failureCount: Int, lastFailureTime: Date?) {
    var lastFailureTime: Long = 0
    fun fail() {
        failureCount++
        lastFailureTime = now()
    }

    fun reset() {
        failureCount = 0
    }

    fun restart() {
        lastFailureTime = now()
    }

    private fun now(): Long = currentTimeMillis()

    fun isWithinDuration(within: Duration): Boolean = (now() - lastFailureTime) < within.toMillis()
}

