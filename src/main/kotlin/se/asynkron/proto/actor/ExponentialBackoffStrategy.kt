package proto.actor

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

open class ExponentialBackoffStrategy(private val backoffWindow: Duration, private val initialBackoff: Duration) : ISupervisorStrategy {
    private val random: Random = Random()

    override fun handleFailure(supervisor: ISupervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        setFailureCount(rs)
        val backoff: Long = rs.failureCount * initialBackoff.toNanos()
        val noise: Int = random.nextInt(500)
        val duration: Duration = Duration.ofMillis(toMilliseconds(backoff + noise))
        launch(CommonPool) {
            delay(duration.toNanos(),TimeUnit.NANOSECONDS)
            supervisor.restartChildren(reason, child)
        }
    }

    private fun toMilliseconds(nanoseconds: Long): Long {
        return nanoseconds / 1000000
    }

    private fun setFailureCount(rs: RestartStatistics) {
        if (rs.isWithinDuration(backoffWindow)) {
            rs.fail()
            return
        }
        rs.reset()
    }
}