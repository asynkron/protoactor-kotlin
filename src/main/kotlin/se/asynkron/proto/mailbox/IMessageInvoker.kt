package proto.mailbox

interface IMessageInvoker {
    suspend fun invokeSystemMessageAsync(msg: SystemMessage): Unit
    suspend fun invokeUserMessageAsync(msg: Any): Unit
    suspend fun escalateFailure(reason: Exception, message: Any)
}