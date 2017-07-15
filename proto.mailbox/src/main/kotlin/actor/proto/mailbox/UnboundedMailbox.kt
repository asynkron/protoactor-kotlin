package actor.proto.mailbox

fun unboundedMailbox(stats: Array<MailboxStatistics> = arrayOf()): Mailbox = DefaultMailbox(UnboundedMailboxQueue(), UnboundedMailboxQueue(), stats)
fun mpscMailbox(capacity: Int = 1000, stats: Array<MailboxStatistics> = arrayOf()): Mailbox = DefaultMailbox(UnboundedMailboxQueue(), MpscQueue(capacity), stats)
