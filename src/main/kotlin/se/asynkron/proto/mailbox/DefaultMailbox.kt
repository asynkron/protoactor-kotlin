package proto.mailbox

import java.util.concurrent.atomic.AtomicInteger

internal class DefaultMailbox(private val systemMessages: IMailboxQueue, private val userMailbox: IMailboxQueue, stats: Array<MailboxStatistics>?) : Mailbox {
    private val stats: Array<MailboxStatistics> = stats ?: arrayOf<MailboxStatistics>() //TODO: reduce allocs
    private val status: AtomicInteger = AtomicInteger(MailboxStatus.Idle)
    private lateinit var dispatcher: Dispatcher
    private lateinit var invoker: MessageInvoker
    private var suspended: Boolean = false

    override fun postUserMessage(msg: Any) {
        userMailbox.push(msg)
        for (stats in stats) stats.messagePosted(msg)
        schedule()
    }

    override fun postSystemMessage(msg: Any) {
        systemMessages.push(msg)
        for (stats in stats) stats.messagePosted(msg)
        schedule()
    }

    override fun registerHandlers(invoker: MessageInvoker, dispatcher: Dispatcher) {
        this.invoker = invoker
        this.dispatcher = dispatcher
    }

    override fun start() {
        for (stats in stats) stats.mailboxStarted()
    }

    private suspend fun runAsync() {
        processMessages()

        status.set(MailboxStatus.Idle)
        if (systemMessages.hasMessages || (!suspended && userMailbox.hasMessages)) {
            schedule()
        } else {
            for (stat in stats) stat.mailboxEmpty()
        }
    }

    private suspend fun processMessages() {
        var msg: Any? = null
        try {
            for (i in 0..dispatcher.throughput) {
                msg = systemMessages.pop()
                if (msg != null) {
                    when (msg) {
                        is SuspendMailbox -> suspended = true
                        is ResumeMailbox -> suspended = false
                    }
                    invoker.invokeSystemMessageAsync(msg as SystemMessage)
                    for (stat in stats) stat.messageReceived(msg)
                }
                if (!suspended) {
                    msg = userMailbox.pop()
                    when (msg) {
                        null -> return
                        else -> {
                            invoker.invokeUserMessageAsync(msg)
                            for (stat in stats) stat.messageReceived(msg)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            if (msg != null) invoker.escalateFailure(e, msg)
        }
    }

    private fun schedule() {
        val wasIdle = status.compareAndSet(MailboxStatus.Idle, MailboxStatus.Busy)
        if (wasIdle) {
            dispatcher.schedule({ -> runAsync() })
        }
    }
}


