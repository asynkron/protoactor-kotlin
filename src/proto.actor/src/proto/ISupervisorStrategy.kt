package proto

interface ISupervisorStrategy {
    fun handleFailure(supervisor: ISupervisor, child: PID, rs: RestartStatistics, cause: Exception)
}