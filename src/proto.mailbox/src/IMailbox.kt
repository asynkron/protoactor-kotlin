package proto.mailbox

interface IMailbox {
    fun postUserMessage (msg : Any)
    fun postSystemMessage (msg : Any)
    fun registerHandlers (invoker : IMessageInvoker, dispatcher : IDispatcher)
    fun start ()
}