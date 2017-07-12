package proto.router

import proto.actor.LocalProcess
import proto.actor.MessageEnvelope
import proto.actor.PID
import proto.mailbox.Mailbox
import proto.router.messages.RouterManagementMessage
import proto.router.routers.RouterState

open class RouterProcess(state: RouterState, mailbox: Mailbox) : LocalProcess(mailbox) {
    private val _state : RouterState = state
    override fun sendUserMessage (pid : PID, message : Any) {
        val msg = when(message){
            is MessageEnvelope -> message.message
            else -> message
        }
        val tmp = msg
        when (tmp) {
            is RouterManagementMessage -> {
                super.sendUserMessage(pid, message)
            }
            else -> {
                _state.routeMessage(message)
            }
        }
    }
}

