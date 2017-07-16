package actor.proto.mailbox

interface MessageInvoker {
    suspend fun invokeSystemMessage(msg: SystemMessage): Unit
    suspend fun invokeUserMessage(msg: Any): Unit
    suspend fun escalateFailure(reason: Exception, message: Any)
}