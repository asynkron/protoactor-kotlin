package proto.actor

import proto.mailbox.SystemMessage

class DeadLetterEvent(val pid: PID, val message: Any, val sender: PID?)

open class DeadLetterProcess : Process() {
    companion object {
        val Instance: DeadLetterProcess = DeadLetterProcess()
    }

    override fun sendUserMessage(pid: PID, message: Any) {
        val (msg, sender, _) = MessageEnvelope.unwrap(message)
        EventStream.Instance.publish(DeadLetterEvent(pid, msg, sender))
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {
        EventStream.Instance.publish(DeadLetterEvent(pid, message, null))
    }
}

