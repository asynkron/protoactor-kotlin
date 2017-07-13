package proto.actor

import kotlinx.coroutines.experimental.Deferred
import proto.mailbox.SystemMessage
import java.time.Duration

class FutureProcess<T>(timeout: Duration? = null) : Process() {

    val pid: PID
    val cd: CompletableDeferred<T>? = null
    override fun sendUserMessage(pid: PID, message: Any) {
        val m = when (message){
            is MessageEnvelope -> message.message
            else -> message
        }
        cd!!.set(m)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {}

    fun deferred(): Deferred<T> {
        return cd!!
    }

    init {
        val name: String = ProcessRegistry.nextId()
        val pid = ProcessRegistry.add(name, this)
        this.pid = pid
    }
}

abstract class CompletableDeferred<out T> : Deferred<T> {
    abstract fun <T> set(value: T)
}