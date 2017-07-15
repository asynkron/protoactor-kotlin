package actor.proto

interface SupervisorStrategy {
    fun handleFailure(supervisor: Supervisor, child: Protos.PID, rs: RestartStatistics, reason: Exception)
}