package proto.mailbox

interface IMessageInvoker {
    suspend fun invokeSystemMessageAsync(msg: Any): Unit
    suspend fun invokeUserMessageAsync(msg: Any): Unit
    suspend fun escalateFailure(reason: Exception, message: Any)
}