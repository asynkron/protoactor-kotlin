package proto.actor

import proto.mailbox.UnboundedMailbox
import java.time.Duration

class PID(val address: String, val id: String) {
    private var _p: Process? = null
    internal fun ref(): Process? {
        throw Exception("Not implemented")
        _p = null
    }

    fun tell(message: Any) {
        val reff: Process = ref() ?: ProcessRegistry.instance.get(this)
        reff.sendUserMessage(this, message)
    }

    fun sendSystemMessage(sys: Any) {
        val reff: Process = ref() ?: ProcessRegistry.instance.get(this)
        reff.sendSystemMessage(this, sys)
    }

    fun request(message: Any, sender: PID) {
        val reff: Process = ref() ?: ProcessRegistry.instance.get(this)
        val messageEnvelope: MessageEnvelope = MessageEnvelope(message, sender, null)
        reff.sendUserMessage(this, messageEnvelope)
    }

    fun <T> requestAsync(message: Any, timeout: Duration): Task {
        return requestAsync(message, FutureProcess<T>(timeout))
    }

    fun <T> requestAsync(message: Any): Task = requestAsync(message, FutureProcess<T>())
    private fun <T> requestAsync(message: Any, future: FutureProcess<T>): Task {
        request(message, future.pid)
        return future.task
    }

    fun stop() {
        val reff: Process = ProcessRegistry.instance.get(this)
        reff.stop(this)
    }

    fun toShortString(): String {
        return address + "/" + id
    }
}

