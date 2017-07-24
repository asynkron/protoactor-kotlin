package actor.proto

import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.withTimeout
import java.time.Duration
import java.util.concurrent.TimeUnit

@Suppress("UNUSED_PARAMETER")
class FutureProcess<out T>(val timeout: Duration? = null) : Process() {
    val pid = ProcessRegistry.add(ProcessRegistry.nextId(), this)
    private val cd = CompletableDeferred<T>()
    override fun sendUserMessage(pid: PID, message: Any) {
        val m = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        @Suppress("UNCHECKED_CAST")
        cd.complete(m as T)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {}
    suspend fun get(): T {
        return when (timeout) {
            null -> cd.await()
            else -> withTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS) { cd.await() }
        }
    }
}