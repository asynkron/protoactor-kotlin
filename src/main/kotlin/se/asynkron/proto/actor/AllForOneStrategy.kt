package proto.actor

import java.time.Duration

class AllForOneStrategy(decider: (PID, Exception) -> SupervisorDirective, maxNrOfRetries: Int, withinTimeSpan: Duration?) : ISupervisorStrategy {
    private val _decider: (PID, Exception) -> SupervisorDirective = decider
    private val _maxNrOfRetries: Int = maxNrOfRetries
    private val _withinTimeSpan: Duration? = withinTimeSpan
    override fun handleFailure(supervisor: ISupervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        val directive: SupervisorDirective = _decider(child, reason)
        val tmp = directive
        when (tmp) {
        //TODO: convert
        }
    }

    private fun requestRestartPermission(rs: RestartStatistics): Boolean {
        if (_maxNrOfRetries == 0) {
            return false
        }
        rs.fail()
        if (_withinTimeSpan == null || rs.isWithinDuration(_withinTimeSpan)) {
            return rs.failureCount <= _maxNrOfRetries
        }
        rs.reset()
        return true
    }
}