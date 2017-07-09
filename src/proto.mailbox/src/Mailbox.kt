package proto.mailbox

import java.util.concurrent.atomic.AtomicInteger

open internal class DefaultMailbox(systemMessages: IMailboxQueue, userMailbox: IMailboxQueue, stats: Array<IMailboxStatistics>?) : IMailbox {
    private val _stats: Array<IMailboxStatistics> = stats ?: arrayOf<IMailboxStatistics>() //TODO: reduce allocs
    private val _systemMessages: IMailboxQueue = systemMessages
    private val _userMailbox: IMailboxQueue = userMailbox
    private val _status: AtomicInteger = AtomicInteger(MailboxStatus.Idle)
    private var _dispatcher: IDispatcher? = null
    private var _invoker: IMessageInvoker? = null
    private var _suspended: Boolean = false

    override fun postUserMessage(msg: Any) {
        _userMailbox.push(msg)
        for (stats in _stats) {
            stats.messagePosted(msg)
        }
        schedule()
    }

    override fun postSystemMessage(msg: Any) {
        _systemMessages.push(msg)
        for (stats in _stats) {
            stats.messagePosted(msg)
        }
        schedule()
    }

    override fun registerHandlers(invoker: IMessageInvoker, dispatcher: IDispatcher) {
        _invoker = invoker
        _dispatcher = dispatcher
    }

    override fun start() {
        for (stats in _stats) {
            stats.mailboxStarted()
        }
    }

    private fun runAsync(): Unit {
        processMessages()

        _status.set(MailboxStatus.Idle)
        if (_systemMessages.hasMessages || _suspended && _userMailbox.hasMessages) {
            schedule()
        } else {
            for (stat in _stats) {
                stat.mailboxEmpty()
            }
        }
    }

    private fun processMessages() {
        var msg: Any? = null
        try {
            for (i in 0.._dispatcher!!.throughput) {
                msg = _systemMessages.pop()
                if (msg != null) {
                    if (msg is SuspendMailbox) {
                        _suspended = true
                    } else if (msg is ResumeMailbox) {
                        _suspended = false
                    }
                    _invoker!!.invokeSystemMessageAsync(msg)

                    for (stat in _stats) {
                        stat.messageReceived(msg)
                    }
                }
                if (_suspended) {
                } else {
                    msg = _userMailbox.pop()
                    if (msg != null) {
                        _invoker!!.invokeUserMessageAsync(msg)

                        for (stat in _stats) {
                            stat.messageReceived(msg)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _invoker!!.escalateFailure(e, msg!!)
        }
    }

    protected fun schedule() {
        val wasIdle = _status.compareAndSet(MailboxStatus.Idle, MailboxStatus.Busy)
        if (wasIdle) {
            _dispatcher!!.schedule(this::runAsync)
        }
    }
}


