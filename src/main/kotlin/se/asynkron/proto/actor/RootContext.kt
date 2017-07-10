package proto.actor

import java.time.Duration

class ActorClient : ISenderContext {
    private val _senderMiddleware : ((ISenderContext, PID, MessageEnvelope) -> Task)? = null
    constructor(messageHeader : MessageHeader, middleware : Array<((ISenderContext, PID, MessageEnvelope) -> Task) -> (ISenderContext, PID, MessageEnvelope) -> Task>)  {
        _senderMiddleware = (defaultSender, {inner, outer -> outer(inner)})
        headers = messageHeader
    }
    override val message : Any?
        get() = null
    override val headers : MessageHeader
    private fun defaultSender (context : ISenderContext, target : PID, message : MessageEnvelope) : Task {
        target.tell(message)
        return Actor.Done
    }
    fun tell (target : PID, message : Any) {
        if (_senderMiddleware != null) {
            if (message is MessageEnvelope) {
                _senderMiddleware.invoke(this, target, message)
            } else {
                _senderMiddleware.invoke(this, target, MessageEnvelope(message, null, null))
            }
        } else {
            target.tell(message)
        }
    }
    fun request (target : PID, message : Any, sender : PID) {
        val envelope : MessageEnvelope = MessageEnvelope(message, sender, null)
        tell(target, envelope)
    }
    fun requestAsync (target : PID, message : Any, timeout : Duration) : Task {
        throw Exception()
    }
    fun requestAsync (target : PID, message : Any) : Task {
        throw Exception()
    }
}

