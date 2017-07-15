package actor.proto

import actor.proto.mailbox.SystemMessage
import actor.proto.mailbox.Mailbox

abstract class Process {
    abstract fun sendUserMessage(pid: PID, message: Any)
    open fun stop(pid: PID) = sendSystemMessage(pid, StopInstance)

    abstract fun sendSystemMessage(pid: PID, message: SystemMessage)
}


open class LocalProcess(private val mailbox: Mailbox) : Process() {
    private var isDead: Boolean = false
    override fun sendUserMessage(pid: PID, message: Any) {
        mailbox.postUserMessage(message)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {
        mailbox.postSystemMessage(message)
    }

    override fun stop(pid: PID) {
        super.stop(pid)
        isDead = true
    }
}