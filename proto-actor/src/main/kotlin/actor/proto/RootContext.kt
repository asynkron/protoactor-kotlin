package actor.proto

import kotlinx.coroutines.experimental.runBlocking
import java.time.Duration

class ActorClient(messageHeader: MessageHeader = EmptyMessageHeader, senderMiddleware: List<SenderMiddleware> = listOf()) : SenderContext {

    private val senderMiddleware: Send? = when {
        senderMiddleware.isEmpty() -> null
        else -> senderMiddleware
                .reversed()
                .fold({ ctx, targetPid, envelope -> ContextHelper.defaultSender(ctx, targetPid, envelope) },
                        { inner, outer -> outer(inner!!) })
    }

    override val message: Any?
        get() = null

    override val headers: MessageHeader = messageHeader
    private fun defaultSender(@Suppress("UNUSED_PARAMETER") context: SenderContext, target: PID, message: MessageEnvelope): Unit {
        target.send(message)
    }

    fun send(target: PID, message: Any) = when (senderMiddleware) {
        null -> target.send(message)
        else -> {
            val c = this
            when (message) {
                is MessageEnvelope -> runBlocking { senderMiddleware.invoke(c, target, message) }
                else -> runBlocking { senderMiddleware.invoke(c, target, MessageEnvelope(message, null, null)) }
            }
        }
    }

    fun request(target: PID, message: Any, sender: PID) {
        val envelope = MessageEnvelope(message, sender, null)
        send(target, envelope)
    }

    suspend fun <T> requestAwait(target: PID, message: Any, timeout: Duration): T {
        val deferredProcess = DeferredProcess<T>(timeout)
        request(target,message,deferredProcess.pid)
        return deferredProcess.await()
    }
}

val DefaultActorClient = ActorClient()