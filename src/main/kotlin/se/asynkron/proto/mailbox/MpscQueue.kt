package se.asynkron.proto.mailbox

import org.jctools.queues.atomic.MpscAtomicArrayQueue
import proto.mailbox.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by ROJO01 on 2017-07-12.
 */


class MpscQueue(capacity : Int = 1000) : IMailboxQueue {
    companion object {
        fun create(capacity: Int = 1000, stats: Array<MailboxStatistics> = arrayOf()): Mailbox = DefaultMailbox(MpscQueue(), MpscQueue(capacity), stats)
    }

    private val messages: MpscAtomicArrayQueue<Any> = MpscAtomicArrayQueue(capacity)
    override fun push(message: Any) {
        messages.add(message)
    }

    override fun pop(): Any? {
        return messages.poll()
    }

    override val hasMessages: Boolean
        get() = messages.count() > 0
}
