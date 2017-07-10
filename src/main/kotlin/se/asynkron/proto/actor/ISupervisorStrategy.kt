package proto.actor

interface ISupervisorStrategy {
    fun handleFailure(supervisor: ISupervisor, child: PID, rs: RestartStatistics, reason: Exception)
}