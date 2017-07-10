package proto

import java.time.Duration
import java.util.*

open class RestartStatistics(var failureCount: Int, lastFailureTime: Date) {
    var lastFailureTime: Date? = lastFailureTime
    fun fail() {
        failureCount++
        lastFailureTime = null //TODO DateTime.NOW
    }

    fun reset() {
        failureCount = 0
    }

    fun restart() {
        lastFailureTime = null //TODO DateTime.NOW
    }

    fun isWithinDuration(within: Duration): Boolean = (Date(). - lastFailureTime) < within
}

