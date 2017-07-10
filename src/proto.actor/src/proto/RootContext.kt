package proto

import java.time.Duration

open class ActorClient : ISenderContext {
    private val _senderMiddleware : (ISenderContext, PID, MessageEnvelope) -> Task
    constructor(messageHeader : MessageHeader, middleware : Array<((ISenderContext, PID, MessageEnvelope) -> Task) -> (ISenderContext, PID, MessageEnvelope) -> Task>)  {
        _senderMiddleware = (SenderdefaultSender, {inner, outer -> outer(inner)})
        headers = messageHeader
    }
    override val message : Any
        get() = null
    override val headers : MessageHeader
    private fun defaultSender (context : ISenderContext, target : PID, message : MessageEnvelope) : Task {
        target.tell(message)
        return Actor.Done
    }
    fun tell (target : PID, message : Any) {
        if (_senderMiddleware != null) {
            if (message is MessageEnvelope) {
                _senderMiddleware(this, target, message)
            } else {
                _senderMiddleware(this, target, MessageEnvelope(message, NullPid, MessageHeader.EmptyHeader))
            }
        } else {
            target.tell(message)
        }
    }
    fun request (target : PID, message : Any, sender : PID) {
        val envelope : MessageEnvelope = MessageEnvelope(message, sender, NullMessageHeader)
        tell(target, envelope)
    }
    fun requestAsync (target : PID, message : Any, timeout : Duration) : Task<T> {
        throw NotImplementedException()
    }
    fun requestAsync (target : PID, message : Any) : Task<T> {
        throw NotImplementedException()
    }
}

