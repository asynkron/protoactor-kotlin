package proto.actor

import kotlinx.coroutines.experimental.Deferred
import java.time.Duration

class PID(val address: String, val id: String) {
    private var _p: Process? = null
    internal fun ref(): Process? {
        return null
    }

    fun tell(message: Any) {
        val reff: Process = ref() ?: ProcessRegistry.get(this)
        reff.sendUserMessage(this, message)
    }

    fun sendSystemMessage(sys: Any) {
        val reff: Process = ref() ?: ProcessRegistry.get(this)
        reff.sendSystemMessage(this, sys)
    }

    fun request(message: Any, sender: PID) {
        val reff: Process = ref() ?: ProcessRegistry.get(this)
        val messageEnvelope: MessageEnvelope = MessageEnvelope(message, sender, null)
        reff.sendUserMessage(this, messageEnvelope)
    }

    suspend fun <T> requestAsync(message: Any, timeout: Duration): T {
        return requestAsync(message, FutureProcess<T>(timeout))
    }

    suspend fun <T> requestAsync(message: Any): T {
        return requestAsync(message, FutureProcess<T>())
    }

    suspend private fun <T> requestAsync(message: Any, future: FutureProcess<T>): T {
        request(message, future.pid)
        return future.deferred().await()
    }

    fun stop() {
        val reff: Process = ProcessRegistry.get(this)
        reff.stop(this)
    }

    fun toShortString(): String {
        return address + "/" + id
    }
}

