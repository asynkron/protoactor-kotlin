package actor.proto

import actor.proto.mailbox.SystemMessage

class DeadLetterEvent(val pid: Protos.PID, val message: Any, val sender: Protos.PID?)

object DeadLetterProcess : Process() {

    override fun sendUserMessage(pid: Protos.PID, message: Any) {
        val dle = when (message) {
            is MessageEnvelope -> DeadLetterEvent(pid, message.message, message.sender)
            else -> DeadLetterEvent(pid, message, null)
        }
        EventStream.publish(dle)

    }

    override fun sendSystemMessage(pid: Protos.PID, message: SystemMessage) {
        EventStream.publish(DeadLetterEvent(pid, message, null))
    }
}

