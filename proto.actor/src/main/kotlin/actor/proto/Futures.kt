package actor.proto

import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.experimental.Deferred
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Suppress("UNUSED_PARAMETER")
class FutureProcess<T>(timeout: Duration? = null) : Process() {
    val pid = ProcessRegistry.add( ProcessRegistry.nextId(), this)
    private var cd = CompletableFuture<T>()
    override fun sendUserMessage(pid: PID, message: Any) {
        val m = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        @Suppress("UNCHECKED_CAST")
        cd.complete(m as T)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {}

    fun future(): Future<T> = cd
}

abstract class CompletableDeferred<out T> : Deferred<T> {
    abstract fun <T> set(value: T)
}