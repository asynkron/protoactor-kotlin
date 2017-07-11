package proto.mailbox

object UnboundedMailbox {
    fun create(stats: Array<MailboxStatistics>): Mailbox = DefaultMailbox(UnboundedMailboxQueue(), UnboundedMailboxQueue(), stats)
    fun create(): Mailbox = DefaultMailbox(UnboundedMailboxQueue(), UnboundedMailboxQueue(), arrayOf())
}