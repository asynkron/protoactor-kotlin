package proto.actor

import proto.mailbox.SystemMessage
import java.time.Duration

infix fun PID.tell(message: Any): Unit {
    this.tell(message)
}

class PID(val address: String, val id: String) {
    internal var _cachedProcess: Process? = null
    internal fun cachedProcess(): Process? {
        if (_cachedProcess == null){
            _cachedProcess = ProcessRegistry.get(this)
        }
        return _cachedProcess
    }

    fun tell(message: Any) {
        val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
        process.sendUserMessage(this, message)
    }

    fun sendSystemMessage(sys: SystemMessage) {
        val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
        process.sendSystemMessage(this, sys)
    }

    fun request(message: Any, sender: PID) {
        val process: Process = cachedProcess() ?: ProcessRegistry.get(this)
        val messageEnvelope: MessageEnvelope = MessageEnvelope(message, sender, null)
        process.sendUserMessage(this, messageEnvelope)
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
        val process:  Process = cachedProcess() ?: ProcessRegistry.get(this)
        process.stop(this)
    }

    fun toShortString(): String {
        return "$address/$id"
    }
}

