package actor.proto.mailbox

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.MessageInvoker

interface Mailbox {
    fun postUserMessage(msg: Any)
    fun postSystemMessage(msg: Any)
    fun registerHandlers(invoker: MessageInvoker, dispatcher: Dispatcher)
    fun start()
}