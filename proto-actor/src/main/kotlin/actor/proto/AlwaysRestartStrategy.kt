package actor.proto

class AlwaysRestartStrategy() : SupervisorStrategy {
    override fun handleFailure(supervisor: Supervisor, child: PID, rs: RestartStatistics, reason: Exception) {
        supervisor.restartChildren(reason, child)
    }
}
