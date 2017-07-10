package proto.actor

import proto.mailbox.IMailbox

abstract class Process {
    abstract fun sendUserMessage (pid : PID, message : Any)
    open fun stop (pid : PID) {
        sendSystemMessage(pid, Stop())
    }
    abstract fun sendSystemMessage (pid : PID, message : Any)
}


open class LocalProcess(val mailbox: IMailbox) : Process() {
    internal var isDead : Boolean = false
    override fun sendUserMessage (pid : PID, message : Any) {
        mailbox.postUserMessage(message)
    }
    override fun sendSystemMessage (pid : PID, message : Any) {
        mailbox.postSystemMessage(message)
    }
    override fun stop (pid : PID) {
        super.stop(pid)
        isDead = true
    }
}

