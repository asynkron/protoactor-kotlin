package proto.actor

import java.time.Duration

class FutureProcess<T> : Process {
    constructor(timeout : Duration)  {
        val name : String = ProcessRegistry.instance.nextId()
        val (pid, absent) = ProcessRegistry.instance.tryAdd(name, this)
        if (absent) {
            throw ProcessNameExistException(name)
        }
        this.pid = pid
       /* if (cts != null) {
            System.Threading.Tasks.Task.delay(1, cts.token).continueWith{t ->
                if (_tcs.task.isCompleted) {
                    _tcs.trySetException(TimeoutException("Request didn't receive any Response within the expected time."))
                    pid.stop()
                }
            }

        }
        task = _tcs.task*/
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

