package proto.actor

import proto.mailbox.SystemMessage

class DeadLetterEvent(val pid: PID, val message: Any, val sender: PID?)

open class DeadLetterProcess : Process() {
    companion object {
        val Instance: DeadLetterProcess = DeadLetterProcess()
    }

    override fun sendUserMessage(pid: PID, message: Any) {
        val dle = when (message){
            is MessageEnvelope -> DeadLetterEvent(pid, message.message, message.sender)
            else -> DeadLetterEvent(pid, message, null)
        }
        EventStream.publish(dle)

    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {
        EventStream.publish(DeadLetterEvent(pid, message, null))
    }
}

