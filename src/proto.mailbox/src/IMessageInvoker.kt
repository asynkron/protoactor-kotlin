package proto.mailbox

interface IMessageInvoker {
    fun invokeSystemMessageAsync (msg : Any) : Task
    fun invokeUserMessageAsync (msg : Any) : Task
    fun escalateFailure (reason : Exception, message : Any)
}