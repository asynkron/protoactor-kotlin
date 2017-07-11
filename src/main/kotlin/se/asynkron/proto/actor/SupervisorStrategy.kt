package proto.actor

interface SupervisorStrategy {
    fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception)
}