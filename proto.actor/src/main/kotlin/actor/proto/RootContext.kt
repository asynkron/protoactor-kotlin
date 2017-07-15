package actor.proto

@Suppress("unused")
class ActorClient(messageHeader: MessageHeader, @Suppress("UNUSED_PARAMETER") middleware: Array<((SenderContext, PID, MessageEnvelope) -> Unit) -> (SenderContext, PID, MessageEnvelope) -> Unit>) : SenderContext {
    private val _senderMiddleware: ((SenderContext, PID, MessageEnvelope) -> Unit)? = null

    override val message: Any?
        get() = null
    override val headers: MessageHeader = messageHeader
    private fun defaultSender(@Suppress("UNUSED_PARAMETER") context: SenderContext, target: PID, message: MessageEnvelope): Unit {
        target.tell(message)
    }

    fun tell(target: PID, message: Any) {
        when (_senderMiddleware) {
            null -> target.tell(message)
            else -> when (message) {
                is MessageEnvelope -> _senderMiddleware.invoke(this, target, message)
                else -> _senderMiddleware.invoke(this, target, MessageEnvelope(message, null, null))
            }
        }
    }

    fun request(target: PID, message: Any, sender: PID) {
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

