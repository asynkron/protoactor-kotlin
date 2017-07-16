package actor.proto.remote

import actor.proto.mailbox.*
import java.util.concurrent.atomic.AtomicInteger

internal object MailboxStatus {
    val Idle : Int = 0
    val Busy : Int = 1
}

open class EndpointWriterMailbox(private val batchSize: Int) : Mailbox {
    private val systemMessages: IMailboxQueue = UnboundedMailboxQueue()
    private val userMessages: IMailboxQueue = UnboundedMailboxQueue()
    private lateinit var dispatcher: Dispatcher
    private lateinit var invoker: MessageInvoker
    private val status : AtomicInteger = AtomicInteger(MailboxStatus.Idle)
    private var suspended: Boolean = false
    override fun postUserMessage (msg : Any) {
        userMessages.push(msg)
        schedule()
    }
    override fun postSystemMessage (msg : Any) {
        systemMessages.push(msg)
        schedule()
    }
    override fun registerHandlers (invoker : MessageInvoker, dispatcher : Dispatcher) {
        this.invoker = invoker
        this.dispatcher = dispatcher
    }
    override fun start () {    }

    private suspend fun runAsync () {
        var msg : Any? = null
        try  {
            val batch : MutableList<RemoteDeliver> = mutableListOf()
            msg = systemMessages.pop()
            if (msg != null) {
                when (msg) {
                    is SuspendMailbox -> suspended = true
                    is ResumeMailbox -> suspended = false
                }
                invoker.invokeSystemMessageAsync(msg as SystemMessage)
            }
            if (!suspended) {
                batch.clear()
                for(i in 0 until  batchSize) {
                    msg = userMessages.pop()
                    if (msg != null){
                        batch.add(msg as RemoteDeliver)
                        if (batch.count() >= batchSize) {
                            break
                        }
                    }
                }
                if (batch.count() > 0) {
                    msg = batch
                    invoker.invokeUserMessageAsync(batch)
                }
            }
        }
        catch (x : Exception) {
            if (msg != null) invoker.escalateFailure(x, msg)
        }
        status.set(MailboxStatus.Idle)
        if (systemMessages.hasMessages || (!suspended && userMessages.hasMessages)) {
            schedule()
        }
    }
    private fun schedule() {
        val wasIdle = status.compareAndSet(MailboxStatus.Idle, MailboxStatus.Busy)
        if (wasIdle) {
            dispatcher.schedule({ runAsync() })
        }
    }
}

