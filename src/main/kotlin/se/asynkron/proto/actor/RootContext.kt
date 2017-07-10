package proto.actor

import kotlinx.coroutines.experimental.Deferred
import java.time.Duration

class ActorClient : ISenderContext {
    private var _senderMiddleware: ((ISenderContext, PID, MessageEnvelope) -> Unit)? = null

    constructor(messageHeader: MessageHeader, middleware: Array<((ISenderContext, PID, MessageEnvelope) -> Unit) -> (ISenderContext, PID, MessageEnvelope) -> Unit>) {
        //_senderMiddleware = defaultSender, {inner, outer -> outer(inner)})
        headers = messageHeader
    }

    override val message: Any?
        get() = null
    override val headers: MessageHeader
    private fun defaultSender(context: ISenderContext, target: PID, message: MessageEnvelope): Unit {
        target.tell(message)
    }

    fun tell(target: PID, message: Any) {
        if (_senderMiddleware != null) {
            if (message is MessageEnvelope) {
                _senderMiddleware!!.invoke(this, target, message)
            } else {
                _senderMiddleware!!.invoke(this, target, MessageEnvelope(message, null, null))
            }
        } else {
            target.tell(message)
        }
    }

    fun request(target: PID, message: Any, sender: PID) {
        val envelope: MessageEnvelope = MessageEnvelope(message, sender, null)
        tell(target, envelope)
    }

    fun <T> requestAsync(target: PID, message: Any, timeout: Duration): Deferred<T> {
        throw Exception()
    }

    fun <T> requestAsync(target: PID, message: Any): Deferred<T> {
        throw Exception()
    }
}

