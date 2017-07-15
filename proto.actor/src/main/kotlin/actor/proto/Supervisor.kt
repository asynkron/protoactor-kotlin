package actor.proto

interface Supervisor {
    val children: Collection<Protos.PID>
    fun escalateFailure(reason: Exception, who: Protos.PID)
    fun restartChildren(reason: Exception, vararg pids: Protos.PID)
    fun stopChildren(vararg pids: Protos.PID)
    fun resumeChildren(vararg pids: Protos.PID)
}