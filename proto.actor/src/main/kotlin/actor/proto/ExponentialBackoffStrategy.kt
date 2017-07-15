package actor.proto

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class ExponentialBackoffStrategy(private val backoffWindow: Duration, private val initialBackoff: Duration) : SupervisorStrategy {
    private val random: Random = Random()

    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        setFailureCount(rs)
        val backoff: Long = rs.failureCount * initialBackoff.toNanos()
        val noise: Int = random.nextInt(500)
        val duration: Duration = Duration.ofNanos(backoff + noise)
        launch(CommonPool) {
            delay(duration.toNanos(), TimeUnit.NANOSECONDS)
            supervisor.restartChildren(reason, child)
        }
    }

    private fun setFailureCount(rs: RestartStatistics) {
        if (rs.isWithinDuration(backoffWindow)) {
            rs.fail()
            return
        }
        rs.reset()
    }
}