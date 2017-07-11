package proto.actor

import kotlinx.coroutines.experimental.Deferred
import java.time.Duration

class FutureProcess<T>(timeout: Duration? = null) : Process() {

    val pid : PID
    val cd : CompletableDeferred<T>? = null
    override fun sendUserMessage (pid : PID, message : Any) {
        val (msg,sender,header)   = MessageEnvelope.unwrap(message)
        cd!!.set(msg)
    }
    override fun sendSystemMessage (pid : PID, message : Any) {}

    fun  deferred(): Deferred<T> {
        return cd!!
    }

    init {
        val name : String = ProcessRegistry.instance.nextId()
        val (pid, absent) = ProcessRegistry.instance.tryAdd(name, this)
        if (absent) {
            throw ProcessNameExistException(name)
        }
        this.pid = pid
    }
}

abstract class CompletableDeferred<T> : Deferred<T>{
    abstract fun <T> set(value : T)
}