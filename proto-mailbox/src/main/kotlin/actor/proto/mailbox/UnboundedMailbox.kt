package actor.proto.mailbox

import org.jctools.queues.MpscUnboundedArrayQueue
import org.jctools.queues.atomic.MpscAtomicArrayQueue
import java.util.concurrent.ConcurrentLinkedQueue

fun newUnboundedMailbox(stats: Array<MailboxStatistics> = arrayOf()): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), ConcurrentLinkedQueue<Any>(), stats)
fun newMpscAtomicArrayMailbox(capacity: Int = 1000, stats: Array<MailboxStatistics> = arrayOf()): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscAtomicArrayQueue(capacity), stats)
fun newMpscUnboundedArrayMailbox(capacity: Int = 1000, stats: Array<MailboxStatistics> = arrayOf()): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscUnboundedArrayQueue(capacity), stats)
