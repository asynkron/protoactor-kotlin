package actor.proto

import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import java.time.Duration
import java.util.concurrent.TimeUnit

class DeferredProcess<out T>(private val timeout: Duration = Duration.ofMillis(5000)) : Process() {
    val pid = ProcessRegistry.put(ProcessRegistry.nextId(), this)
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

    suspend fun await(): T {
        try {
            val result = withTimeout(timeout.toMillis()) { cd.await() }
            ProcessRegistry.remove(pid)
            return result;
        } catch (exception: Exception) {
            ProcessRegistry.remove(pid)
            throw exception;
        }
    }
}
