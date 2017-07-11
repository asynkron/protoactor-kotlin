package proto.actor

import java.time.Duration

class AllForOneStrategy(private val decider: (PID, Exception) -> SupervisorDirective, private val maxNrOfRetries: Int, private val withinTimeSpan: Duration?) : SupervisorStrategy {
    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        val directive: SupervisorDirective = decider(child, reason)
        when (directive) {
            SupervisorDirective.Resume -> TODO()
            SupervisorDirective.Restart -> TODO()
            SupervisorDirective.Stop -> TODO()
            SupervisorDirective.Escalate -> TODO()
        }
    }

    private fun requestRestartPermission(rs: RestartStatistics): Boolean {
        if (maxNrOfRetries == 0) {
            return false
        }
        rs.fail()
        if (withinTimeSpan == null || rs.isWithinDuration(withinTimeSpan)) {
            return rs.failureCount <= maxNrOfRetries
        }
        rs.reset()
        return true
    }
}