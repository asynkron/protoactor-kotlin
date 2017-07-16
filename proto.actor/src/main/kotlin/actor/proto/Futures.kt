package actor.proto

import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.experimental.Deferred
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

@Suppress("UNUSED_PARAMETER")
class FutureProcess<T>(timeout: Duration? = null) : Process() {

    private val name = ProcessRegistry.nextId()
    val pid = ProcessRegistry.add(name, this)
    private var cd: CompletableFuture<T> = CompletableFuture()
    override fun sendUserMessage(pid: PID, message: Any) {
        val m = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        @Suppress("UNCHECKED_CAST")
        cd.complete(m as T)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {}

    fun future(): Future<T> {
        return cd
    }
}

abstract class CompletableDeferred<out T> : Deferred<T> {
    abstract fun <T> set(value: T)
}