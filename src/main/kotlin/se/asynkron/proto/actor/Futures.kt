package proto.actor

import kotlinx.coroutines.experimental.Deferred
import java.time.Duration

class FutureProcess<T> : Process {

    constructor(timeout : Duration? = null)  {
        val name : String = ProcessRegistry.instance.nextId()
        val (pid, absent) = ProcessRegistry.instance.tryAdd(name, this)
        if (absent) {
            throw ProcessNameExistException(name)
        }
        this.pid = pid
    }
    val pid : PID
    val cd : CompletableDeferred<T> = CompletableDeferred()
    override fun sendUserMessage (pid : PID, message : Any) {
        val (msg,sender,header)   = MessageEnvelope.unwrap(message)
        cd.set(msg)
    }
    override fun sendSystemMessage (pid : PID, message : Any) {
    }

    fun  deferred(): Deferred<T> {
        return cd.deferred;
    }
}

