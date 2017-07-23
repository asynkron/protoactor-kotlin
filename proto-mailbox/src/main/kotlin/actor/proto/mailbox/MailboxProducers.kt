package actor.proto.mailbox

import org.jctools.queues.*
import org.jctools.queues.atomic.*
import java.util.concurrent.ConcurrentLinkedQueue

private val emptyStats : Array<MailboxStatistics> = arrayOf()
fun newUnboundedMailbox(stats: Array<MailboxStatistics> = emptyStats): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), ConcurrentLinkedQueue<Any>(), stats)
fun newMpscAtomicArrayMailbox(capacity: Int = 1000, stats: Array<MailboxStatistics> = emptyStats): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscAtomicArrayQueue(capacity), stats)
fun newMpscArrayMailbox(capacity: Int = 1000, stats: Array<MailboxStatistics> = emptyStats): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscArrayQueue(capacity), stats)
fun newMpscUnboundedArrayMailbox(chunkSize: Int = 5, stats: Array<MailboxStatistics> = emptyStats): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscUnboundedArrayQueue(chunkSize), stats)
fun newMpscGrowableArrayMailbox(initialCapacity: Int = 5, stats: Array<MailboxStatistics> = emptyStats): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscGrowableArrayQueue(initialCapacity,Int.MAX_VALUE), stats)
fun newMpscGrowableAtomicArrayMailbox(initialCapacity: Int = 5, stats: Array<MailboxStatistics> = emptyStats): Mailbox = DefaultMailbox(ConcurrentLinkedQueue<Any>(), MpscGrowableAtomicArrayQueue(initialCapacity,Int.MAX_VALUE), stats)
