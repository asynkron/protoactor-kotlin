package actor.proto

import mu.KotlinLogging
import java.time.Duration
private val logger = KotlinLogging.logger {}
class OneForOneStrategy(private val decider: (PID, Exception) -> SupervisorDirective, private val maxNrOfRetries: Int, private val withinTimeSpan: Duration? = null) : SupervisorStrategy {
    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        val directive: SupervisorDirective = decider(child, reason)
        when (directive) {
            SupervisorDirective.Resume -> supervisor.resumeChildren(child)
            SupervisorDirective.Restart -> {
                if (requestRestartPermission(rs)) {
                    logger.debug("Restarting ${child.toShortString()} Reason $reason")
                    supervisor.restartChildren(reason, child)
                } else {
                    logger.debug("Stopping ${child.toShortString()} Reason $reason")
                    supervisor.stopChildren(child)
                }
            }
            SupervisorDirective.Stop -> {
                logger.debug("Stopping ${child.toShortString()} Reason $reason")
                supervisor.stopChildren(child)
            }
            SupervisorDirective.Escalate -> supervisor.escalateFailure(reason, child)
        }
    }

    private fun requestRestartPermission(rs: RestartStatistics): Boolean {
        return when (maxNrOfRetries) {
            0 -> false
            else -> {
                rs.fail()
                when {
                    withinTimeSpan == null || rs.isWithinDuration(withinTimeSpan) -> rs.failureCount <= maxNrOfRetries
                    else -> {
                        rs.reset()
                        true
                    }
                }
            }
        }
    }
}
