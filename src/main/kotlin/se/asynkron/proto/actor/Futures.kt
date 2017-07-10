package proto.actor

import kotlinx.coroutines.experimental.channels.produce
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
    override fun sendUserMessage (pid : PID, message : Any) {
        val (msg,sender,header)   = MessageEnvelope.unwrap(message)
        if (msg is T) {
            if (_cts != null && _cts.isCancellationRequested) {
                return 
            }
            _tcs.trySetResult(Tenv.message)
            pid.stop()
        } else {
            throw InvalidOperationException("Unexpected message.  Was type ${env.message.getType()} but expected ${T}")
        }
    }
    override fun sendSystemMessage (pid : PID, message : Any) {
    }
}

