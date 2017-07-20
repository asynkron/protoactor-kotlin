package actor.proto.mailbox

interface Mailbox {
    fun postUserMessage(msg: Any)
    fun postSystemMessage(msg: Any)
    fun registerHandlers(invoker: MessageInvoker, dispatcher: Dispatcher)
    fun start()
}