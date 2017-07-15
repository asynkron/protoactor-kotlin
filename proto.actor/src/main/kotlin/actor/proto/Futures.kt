package actor.proto

import actor.proto.mailbox.SystemMessage
import kotlinx.coroutines.experimental.Deferred
import java.time.Duration

@Suppress("UNUSED_PARAMETER")
class FutureProcess<out T>(timeout: Duration? = null) : Process() {

    val pid: PID
    private lateinit var cd: CompletableDeferred<T>
    override fun sendUserMessage(pid: PID, message: Any) {
        val m = when (message) {
            is MessageEnvelope -> message.message
            else -> message
        }
        cd.set(m)
    }

    override fun sendSystemMessage(pid: PID, message: SystemMessage) {}

    fun deferred(): Deferred<T> {
        return cd
    }

    init {
        val name = ProcessRegistry.nextId()
        val pid = ProcessRegistry.add(name, this)
        this.pid = pid
    }
}

abstract class CompletableDeferred<out T> : Deferred<T> {
    abstract fun <T> set(value: T)
}