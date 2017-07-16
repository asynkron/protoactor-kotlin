package proto.remote

import actor.proto.remote.RemoteDeliver

internal object MailboxStatus {
    val Idle : Int = 0
    val Busy : Int = 1
}


open class EndpointWriterMailbox : Mailbox {
    private val _batchSize : Int = 0
    private val _systemMessages : MailboxQueue = UnboundedMailboxQueue()
    private val _userMessages : MailboxQueue = UnboundedMailboxQueue()
    private var _dispatcher : Dispatcher? = null
    private var _invoker : MessageInvoker? = null
    private val _status : Int = MailboxStatus.Idle
    private var _suspended : Boolean = false
    constructor(batchSize : Int)  {
        _batchSize = batchSize
    }
    override fun postUserMessage (msg : Any) {
        _userMessages.push(msg)
        schedule()
    }
    override fun postSystemMessage (msg : Any) {
        _systemMessages.push(msg)
        schedule()
    }
    override fun registerHandlers (invoker : MessageInvoker, dispatcher : Dispatcher) {
        _invoker = invoker
        _dispatcher = dispatcher
    }
    override fun start () {
    }
    private suspend fun runAsync () {
        var m : Any = null
        try  {
            val t : Int = _dispatcher.throughput
            val batch : MutableList<RemoteDeliver> = mutableListOf(_batchSize)
            val sys : Any = _systemMessages.pop()
            if (sys != null) {
                if (sys is SuspendMailbox) {
                    _suspended = true
                }
                if (sys is ResumeMailbox) {
                    _suspended = false
                }
                m = sys
                _invoker.invokeSystemMessageAsync(sys)
            }
            if (!_suspended) {
                batch.clear()
                var msg : Any = 
                while ((msg = _userMessages.pop()) != null) {
                    batch.add(RemoteDelivermsg)
                    if (batch.count >= _batchSize) {
                        break
                    }
                }
                if (batch.count > 0) {
                    m = batch
                    _invoker.invokeUserMessageAsync(batch)
                }
            }
        }
        catch (x : Exception) {
            _invoker.escalateFailure(x, m)
        }
        Interlocked.exchange(_status, MailboxStatus.Idle)
        if (_userMessages.hasMessages || _systemMessages.hasMessages) {
            schedule()
        }
    }
    protected fun schedule () {
        if (Interlocked.exchange(_status, MailboxStatus.Busy) == MailboxStatus.Idle) {
            _dispatcher.schedule(runAsync)
        }
    }
}

