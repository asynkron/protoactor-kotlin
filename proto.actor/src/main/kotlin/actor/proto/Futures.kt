package actor.proto

import actor.proto.mailbox.SystemMessage
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Suppress("UNUSED_PARAMETER")
class FutureProcess<out T>(val timeout: Duration? = null) : Process() {
    val pid = ProcessRegistry.add(ProcessRegistry.nextId(), this)
    private val cd = CompletableFuture<T>()
    override fun sendUserMessage(pid: PID, message: Any) {
        val m = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        @Suppress("UNCHECKED_CAST")
        cd.complete(m as T)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {}
    fun get(): T {
        return when (timeout) {
            null -> cd.get()
            else -> cd.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
        }
    }
}