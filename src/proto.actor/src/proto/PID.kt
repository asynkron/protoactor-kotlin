package proto

import java.time.Duration

val NullPid = PID("","")

class PID(val address: String, val id: String) {
    private var _p : Process? = null
    internal val ref : Process? = null
    fun tell (message : Any) {
        val reff : Process = ref ?: ProcessRegistry.instance.get(this)
        reff.sendUserMessage(this, message)
    }
    fun sendSystemMessage (sys : Any) {
        val reff : Process = ref ?: ProcessRegistry.instance.get(this)
        reff.sendSystemMessage(this, sys)
    }
    fun request (message : Any, sender : PID) {
        val reff : Process = ref ?: ProcessRegistry.instance.get(this)
        val messageEnvelope : MessageEnvelope = MessageEnvelope(message, sender, null)
        reff.sendUserMessage(this, messageEnvelope)
    }
    fun requestAsync (message : Any, timeout : Duration) : Task<T> = requestAsync(message, FutureProcess<T>(timeout))
    fun requestAsync (message : Any) : Task<T> = requestAsync(message, FutureProcess<T>())
    private fun requestAsync (message : Any, future : FutureProcess<T>) : Task<T> {
        request(message, future.pid)
        return future.task
    }
    fun stop () {
        val reff : Process = ProcessRegistry.instance.get(this)
        reff.stop(this)
    }
    fun toShortString () : String {
        return address + "/" + id
    }
}

