package proto.mailbox

interface IMessageInvoker {
    fun invokeSystemMessageAsync (msg : Any) : Unit
    fun invokeUserMessageAsync (msg : Any) : Unit
    fun escalateFailure (reason : Exception, message : Any)
}