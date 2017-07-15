package proto.router

import proto.actor.LocalProcess
import proto.actor.MessageEnvelope
import proto.actor.PID
import proto.mailbox.Mailbox
import proto.router.routers.RouterState
import se.asynkron.proto.router.RouterManagementMessage

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

