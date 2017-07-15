package actor.proto.router

import actor.proto.LocalProcess
import actor.proto.MessageEnvelope
import actor.proto.PID
import proto.mailbox.Mailbox


class RouterProcess(private val state: RouterState, mailbox: Mailbox) : LocalProcess(mailbox) {
    override fun sendUserMessage(pid: PID, message: Any) {
        val unwrapped = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        when (unwrapped) {
            is RouterManagementMessage -> super.sendUserMessage(pid, message)
            else -> state.routeMessage(message)
        }
    }
}

