package actor.proto.remote

import actor.proto.mailbox.*
import org.jctools.queues.MpscUnboundedArrayQueue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

open class EndpointWriterMailbox(private val batchSize: Int) : Mailbox {


    private val systemMessages: MailboxQueue = ConcurrentLinkedQueue<Any>()
    private val userMessages: MailboxQueue = MpscUnboundedArrayQueue<Any>(200)
    private lateinit var dispatcher: Dispatcher
    private lateinit var invoker: MessageInvoker
    private val status: AtomicInteger = AtomicInteger(MailboxStatus.IDLE)
    private var suspended: Boolean = false
    override fun postUserMessage(msg: Any) {
        userMessages.add(msg)
        schedule()
    }

    override fun postSystemMessage(msg: Any) {
        systemMessages.add(msg)
        schedule()
    }

    override fun registerHandlers(invoker: MessageInvoker, dispatcher: Dispatcher) {
        this.invoker = invoker
        this.dispatcher = dispatcher
    }

    override fun start() {}

    private val batch: MutableList<RemoteDeliver> = mutableListOf()
    private suspend fun run() {
        var msg: Any? = null
        try {

            if (systemMessages.isNotEmpty()) {
                msg = systemMessages.poll()
                if (msg != null) {
                    when (msg) {
                        is SuspendMailbox -> suspended = true
                        is ResumeMailbox -> suspended = false
                    }
                    invoker.invokeSystemMessage(msg as SystemMessage)
                }
            }
            if (!suspended) {
                batch.clear() //move to end?
                for (i in 0 until batchSize) {
                    msg = userMessages.poll()
                    if (msg != null) {
                        batch.add(msg as RemoteDeliver)
                        if (batch.count() >= batchSize) {
                            break
                        }
                    }
                }
                if (batch.isNotEmpty()) {
                    msg = batch
                    invoker.invokeUserMessage(batch)
                }
            }
        } catch (x: Exception) {
            if (msg != null) invoker.escalateFailure(x, msg)
        }
        status.set(MailboxStatus.IDLE)
        if (systemMessages.isNotEmpty() || (!suspended && userMessages.isNotEmpty())) {
            schedule()
        }
    }

    private fun schedule() {
        val wasIdle = status.compareAndSet(MailboxStatus.IDLE, MailboxStatus.BUSY)
        if (wasIdle) {
            dispatcher.schedule({ run() })
        }
    }
}

