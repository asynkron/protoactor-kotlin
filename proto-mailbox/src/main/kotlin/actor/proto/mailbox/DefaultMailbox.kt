package actor.proto.mailbox

import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private val emptyStats = arrayOf<MailboxStatistics>()
typealias MailboxQueue = Queue<Any>
class DefaultMailbox(private val systemMessages: MailboxQueue, private val userMailbox: MailboxQueue, private val stats: Array<MailboxStatistics> = emptyStats) : Mailbox {
    private val status = AtomicInteger(MailboxStatus.IDLE)
    private val sysCount = AtomicInteger(0)
    private lateinit var dispatcher: Dispatcher
    private lateinit var invoker: MessageInvoker
    private var suspended: Boolean = false

    override fun postUserMessage(msg: Any) {
        userMailbox.offer (msg)
        for (stats in stats) stats.messagePosted(msg)
        schedule()
    }

    override fun postSystemMessage(msg: Any) {
        sysCount.incrementAndGet()
        systemMessages.add(msg)
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

    override suspend fun run() {
        var msg: Any? = null
        try {
            for (i in 0 until dispatcher.throughput) {

                if (sysCount.get() > 0) {
                    msg = systemMessages.poll()
                    sysCount.decrementAndGet()

                    if (msg != null) {
                        when (msg) {
                            is SuspendMailbox -> suspended = true
                            is ResumeMailbox -> suspended = false
                        }
                        invoker.invokeSystemMessage(msg as SystemMessage)
                        for (stat in stats) stat.messageReceived(msg)
                    }
                }
                if (!suspended) {
                    msg = userMailbox.poll()
                    if (msg == null) break
                    else {
                        invoker.invokeUserMessage(msg)
                        for (stat in stats) stat.messageReceived(msg)
                    }
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            if (msg != null) invoker.escalateFailure(e, msg)
        }

        status.set(MailboxStatus.IDLE)
        if (systemMessages.isNotEmpty() || (!suspended && userMailbox.isNotEmpty())) {
            schedule()
        } else {
            for (stat in stats) stat.mailboxEmpty()
        }
    }

    private val r: suspend () -> Unit = { run() }
    private fun schedule() {
        val wasIdle = status.compareAndSet(MailboxStatus.IDLE, MailboxStatus.BUSY)
        if (wasIdle) {
            dispatcher.schedule(this)
        }
    }
}


