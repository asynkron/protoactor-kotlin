package actor.proto.mailbox

import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private val emptyStats = arrayOf<MailboxStatistics>()
typealias MailboxQueue = Queue<Any>
class DefaultMailbox(private val systemMessages: MailboxQueue, private val userMailbox: MailboxQueue, private val stats: Array<MailboxStatistics> = emptyStats) : Mailbox {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultMailbox::class.java)
    }

    private val status = AtomicInteger(MailboxStatus.IDLE)
    private val sysCount = AtomicInteger(0)
    private val userCount = AtomicInteger(0)
    private lateinit var dispatcher: Dispatcher
    private lateinit var invoker: MessageInvoker
    private var suspended: Boolean = false

    fun status(): Int = status.get()

    override fun postUserMessage(msg: Any) {
        if (userMailbox.offer(msg)) {
            userCount.incrementAndGet()
            schedule()
            for (stats in stats) stats.messagePosted(msg)
        } else {
            for (stats in stats) stats.messageDropped(msg)
        }
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
                if (!suspended && userCount.get() > 0) {
                    msg = userMailbox.poll()
                    userCount.decrementAndGet()
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
//        if (userCount.get()==0 && userMailbox.isNotEmpty()) {
//            LOGGER.warn("Mailbox is not empty, but count is zero")
//        }
        if (sysCount.get() > 0 || (!suspended && userCount.get() > 0)) {
            schedule()
        } else {
            for (stat in stats) stat.mailboxEmpty()
            //debug
//            if (systemMessages.isNotEmpty() || (!suspended && userMailbox.isNotEmpty())) {
//                LOGGER.warn("isNotEmpty check, but atomic counter shows no messages")
//                LOGGER.info("Size of system mailbox" + systemMessages.size)
//                LOGGER.info("Size of user mailbox" + userMailbox.size)
//                LOGGER.info("System Messages is not Empty: " + systemMessages.isNotEmpty().toString())
//                LOGGER.info("User Mailbox is not Empty: " + userMailbox.isNotEmpty().toString())
//                LOGGER.info("System message count " + sysCount)
//                LOGGER.info("User mailbox count " + userCount)
//            }
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


