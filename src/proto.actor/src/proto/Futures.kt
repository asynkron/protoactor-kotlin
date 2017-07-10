package proto

open internal class FutureProcess : Process {
    private val _cts : CancellationTokenSource
    private val _tcs : TaskCompletionSource<T>
    constructor(timeout : Duration)  {
    }
    constructor(cancellationToken : CancellationToken)  {
    }
    constructor()  {
    }
    constructor(cts : CancellationTokenSource)  {
        _tcs = TaskCompletionSource<T>()
        _cts = cts
        var name : String = ProcessRegistry.instance.nextId()
        var (pid, absent) = ProcessRegistry.instance.tryAdd(name, this)
        if (absent) {
            throw ProcessNameExistException(name)
        }
        pid = pid
        if (cts != null) {
            System.Threading.Tasks.Task.delay(1, cts.token).continueWith{t -> 
                if (_tcs.task.isCompleted) {
                    _tcs.trySetException(TimeoutException("Request didn't receive any Response within the expected time."))
                    pid.stop()
                }
            }

        }
        task = _tcs.task
    }
    val pid : PID
    val task : Task<T>
    protected internal fun sendUserMessage (pid : PID, message : Any) {
        var env :  = MessageEnvelope.unwrap(message)
        if (env.message is T || message == null) {
            if (_cts != null && _cts.isCancellationRequested) {
                return 
            }
            _tcs.trySetResult(Tenv.message)
            pid.stop()
        } else {
            throw InvalidOperationException("Unexpected message.  Was type ${env.message.getType()} but expected ${T}")
        }
    }
    protected internal fun sendSystemMessage (pid : PID, message : Any) {
    }
}

