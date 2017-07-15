package actor.proto

import java.time.Duration

class AllForOneStrategy(private val decider: (PID, Exception) -> SupervisorDirective, private val maxNrOfRetries: Int, private val withinTimeSpan: Duration?) : SupervisorStrategy {
    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        val directive: SupervisorDirective = decider(child, reason)
        when (directive) {
            SupervisorDirective.Resume -> {
                Logger.logInformation("Resuming ${child.toShortString()} Reason $reason")
                supervisor.resumeChildren(child)
            }
            SupervisorDirective.Restart -> {
                when {
                    requestRestartPermission(rs) -> {
                        Logger.logInformation("Restarting ${child.toShortString()} Reason $reason")
                        supervisor.restartChildren(reason, *supervisor.children.toTypedArray())
                    }
                    else -> {
                        Logger.logInformation("Stopping ${child.toShortString()} Reason $reason")
                        supervisor.stopChildren(*supervisor.children.toTypedArray())
                    }
                }
            }
            SupervisorDirective.Stop -> {
                Logger.logInformation("Stopping ${child.toShortString()} Reason $reason")
                supervisor.stopChildren(*supervisor.children.toTypedArray())
            }
            SupervisorDirective.Escalate -> {
                supervisor.escalateFailure(reason, child)
            }
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