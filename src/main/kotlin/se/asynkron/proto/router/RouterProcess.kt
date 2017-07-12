package proto.router

import proto.actor.LocalProcess
import proto.actor.MessageEnvelope
import proto.actor.PID
import proto.mailbox.Mailbox
import proto.router.routers.RouterState
import se.asynkron.proto.router.RouterManagementMessage

open class RouterProcess(val state: RouterState, mailbox: Mailbox) : LocalProcess(mailbox) {
    override fun sendUserMessage(pid: PID, message: Any) {
        val msg = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        val tmp = msg
        when (tmp) {
            is RouterManagementMessage -> {
                super.sendUserMessage(pid, message)
            }
            else -> {
                state.routeMessage(message)
            }
        }
    }
}

