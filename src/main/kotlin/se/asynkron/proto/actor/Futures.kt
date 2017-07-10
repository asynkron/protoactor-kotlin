package proto.actor

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.channels.produce
import java.time.Duration
import kotlin.coroutines.experimental.CoroutineContext

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
    override fun sendUserMessage (pid : PID, message : Any) {
        val (msg,sender,header)   = MessageEnvelope.unwrap(message)

    }
    override fun sendSystemMessage (pid : PID, message : Any) {
    }

    fun  deferred(): Deferred<T> {
        return null;
    }
}

