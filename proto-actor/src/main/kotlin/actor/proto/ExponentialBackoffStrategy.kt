package actor.proto


import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*

class ExponentialBackoffStrategy(private val backoffWindow: Duration, private val initialBackoff: Duration) : SupervisorStrategy {
    private val random: Random = Random()

    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        setFailureCount(rs)
        val backoff: Long = rs.failureCount * initialBackoff.toNanos()
        val noise: Int = random.nextInt(500)
        val duration: Duration = Duration.ofNanos(backoff + noise)
        GlobalScope.launch {
            delay(duration.toMillis())
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
