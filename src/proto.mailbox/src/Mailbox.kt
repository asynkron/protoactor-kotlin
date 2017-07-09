package proto.mailbox

import java.util.concurrent.atomic.AtomicInteger

open internal class DefaultMailbox : IMailbox {
    private val _stats : Array<IMailboxStatistics>
    private val _systemMessages : IMailboxQueue
    private val _userMailbox : IMailboxQueue
    private var _dispatcher : IDispatcher? = null
    private var _invoker : IMessageInvoker? = null
    private var _status : AtomicInteger = AtomicInteger(MailboxStatus.Idle)
    private var _suspended : Boolean = false

    constructor(systemMessages : IMailboxQueue, userMailbox : IMailboxQueue, stats : Array<IMailboxStatistics>)  {
        _systemMessages = systemMessages
        _userMailbox = userMailbox
        _stats = stats
    }
    override fun postUserMessage (msg : Any) {
        _userMailbox.push(msg)
        for (stats in _stats) {
            stats.messagePosted(msg)
        }
        schedule()
    }
    override fun postSystemMessage (msg : Any) {
        _systemMessages.push(msg)
        for (stats in _stats) {
            stats.messagePosted(msg)
        }
        schedule()
    }
    override fun registerHandlers (invoker : IMessageInvoker, dispatcher : IDispatcher) {
        _invoker = invoker
        _dispatcher = dispatcher
    }
    override fun start () {
        for (stats in _stats) {
            stats.mailboxStarted()
        }
    }
    private fun runAsync () : Task {
        val done : Boolean = processMessages()
        if (done)
            return Task.fromResult(0)

        _status.set(MailboxStatus.Idle)
        if (_systemMessages.hasMessages || _suspended && _userMailbox.hasMessages) {
            schedule()
        }
        return Task.fromResult(0)
    }
    private fun processMessages () : Boolean {
        var msg : Any? = null
        try  {
            for () {
                if (msg = _systemMessages.pop() != null) {
                    if (msg is SuspendMailbox) {
                        _suspended = true
                    }
                    var t : Task = _invoker.invokeSystemMessageAsync(msg)
                    if (t.isFaulted) {
                        _invoker.escalateFailure(t.exception, msg)
                    }
                    if (t.isCompleted) {
                        t.continueWith(rescheduleOnTaskComplete, msg)
                        return false
                    }
                    for () {
                        _stats[si].messageReceived(msg)
                    }
                }
                if (_suspended) {
                }
                if (msg = _userMailbox.pop() != null) {
                    var t : Task = _invoker.invokeUserMessageAsync(msg)
                    if (t.isFaulted) {
                        _invoker.escalateFailure(t.exception, msg)
                    }
                    if (t.isCompleted) {
                        t.continueWith(rescheduleOnTaskComplete, msg)
                        return false
                    }
                    for () {
                        _stats[si].messageReceived(msg)
                    }
                }
            }
        }
        catch (e : Exception) {
            _invoker.escalateFailure(e, msg)
        }
        return true
    }
    private fun rescheduleOnTaskComplete (task : Task, message : Any) {
        if (task.isFaulted) {
            _invoker.escalateFailure(task.exception, message)
        }
        _dispatcher.schedule(this::runAsync)
    }
    protected fun schedule () {
        val wasIdle = _status.compareAndSet(MailboxStatus.Idle,MailboxStatus.Busy)
        if (wasIdle) {
            _dispatcher?.schedule(this::runAsync)
        }
    }
}


