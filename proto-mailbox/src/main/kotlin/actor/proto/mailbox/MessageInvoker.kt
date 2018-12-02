package actor.proto.mailbox

interface MessageInvoker {
    suspend fun invokeSystemMessage(msg: SystemMessage)
    suspend fun invokeUserMessage(msg: Any)
    suspend fun escalateFailure(reason: Exception, message: Any)
}
