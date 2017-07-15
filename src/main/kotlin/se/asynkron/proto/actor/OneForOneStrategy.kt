package proto.actor

import java.time.Duration

class OneForOneStrategy(private val decider: (PID, Exception) -> SupervisorDirective, private val maxNrOfRetries: Int, private val withinTimeSpan: Duration?) : SupervisorStrategy {
    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        val directive: SupervisorDirective = decider(child, reason)
        when (directive) {
            SupervisorDirective.Resume -> supervisor.resumeChildren(child)
            SupervisorDirective.Restart -> {
                if (requestRestartPermission(rs)) {
                    Logger.logInformation("Restarting ${child.toShortString()} Reason $reason")
                    supervisor.restartChildren(reason, child)
                } else {
                    Logger.logInformation("Stopping ${child.toShortString()} Reason $reason")
                    supervisor.stopChildren(child)
                }
            }
            SupervisorDirective.Stop -> {
                Logger.logInformation("Stopping ${child.toShortString()} Reason $reason")
                supervisor.stopChildren(child)
            }
            SupervisorDirective.Escalate -> supervisor.escalateFailure(reason, child)
        }
    }

    private fun requestRestartPermission(rs: RestartStatistics): Boolean {
        if (maxNrOfRetries == 0) return false
        rs.fail()
        if (withinTimeSpan == null || rs.isWithinDuration(withinTimeSpan)) return rs.failureCount <= maxNrOfRetries
        rs.reset()
        return true
    }
}

object Logger {
    fun logInformation(message:String){}
}
