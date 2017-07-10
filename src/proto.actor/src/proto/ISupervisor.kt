package proto

interface ISupervisor {
    val children: Collection<PID>
    fun escalateFailure(reason: Exception, who: PID)
    fun restartChildren(reason: Exception,vararg pids: Array<PID>)
    fun stopChildren(vararg pids: Array<PID>)
    fun resumeChildren(vararg pids: Array<PID>)
}