package proto.mailbox

object UnboundedMailbox {
    fun create(stats: Array<IMailboxStatistics>): IMailbox = DefaultMailbox(UnboundedMailboxQueue(), UnboundedMailboxQueue(), stats)
    fun create(): IMailbox = DefaultMailbox(UnboundedMailboxQueue(), UnboundedMailboxQueue(), arrayOf())
}