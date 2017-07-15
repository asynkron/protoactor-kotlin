package actor.proto

@Suppress("unused")
class ActorClient(messageHeader: MessageHeader, @Suppress("UNUSED_PARAMETER") middleware: Array<((SenderContext, Protos.PID, MessageEnvelope) -> Unit) -> (SenderContext, Protos.PID, MessageEnvelope) -> Unit>) : SenderContext {
    private val _senderMiddleware: ((SenderContext, Protos.PID, MessageEnvelope) -> Unit)? = null

    override val message: Any?
        get() = null
    override val headers: MessageHeader = messageHeader
    private fun defaultSender(@Suppress("UNUSED_PARAMETER") context: SenderContext, target: Protos.PID, message: MessageEnvelope): Unit {
        target.tell(message)
    }

    fun tell(target: Protos.PID, message: Any) {
        when (_senderMiddleware) {
            null -> target.tell(message)
            else -> when (message) {
                is MessageEnvelope -> _senderMiddleware.invoke(this, target, message)
                else -> _senderMiddleware.invoke(this, target, MessageEnvelope(message, null, null))
            }
        }
    }

    fun request(target: Protos.PID, message: Any, sender: Protos.PID) {
        val envelope = MessageEnvelope(message, sender, null)
        tell(target, envelope)
    }

//    suspend fun <T> requestAsync(target: PID, message: Any, timeout: Duration): T {
//        throw Exception()
//    }
//
//    suspend fun <T> requestAsync(target: PID, message: Any): T {
//        throw Exception()
//    }
}

