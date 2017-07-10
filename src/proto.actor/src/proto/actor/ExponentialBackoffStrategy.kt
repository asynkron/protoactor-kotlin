package proto.actor

import java.time.Duration
import java.util.*

open class ExponentialBackoffStrategy(backoffWindow: Duration, initialBackoff: Duration) : ISupervisorStrategy {
    private val _backoffWindow: Duration = backoffWindow
    private val _initialBackoff: Duration = initialBackoff
    private val _random: Random = Random()

    override fun handleFailure(supervisor: ISupervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        setFailureCount(rs)
        val backoff: Long = rs.failureCount * _initialBackoff.toNanos()
        val noise: Int = _random.nextInt(500)
        val duration: Duration = Duration.ofMillis(toMilliseconds(backoff + noise))
        /*Task.delay(duration).continueWith { t ->
            supervisor.restartChildren(reason, arrayOf(child))
        }*/

    }

    private fun toMilliseconds(nanoseconds: Long): Long {
        return nanoseconds / 1000000
    }

    private fun setFailureCount(rs: RestartStatistics) {
        if (rs.isWithinDuration(_backoffWindow)) {
            rs.fail()
            return
        }
        rs.reset()
    }
}