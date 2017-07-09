package proto.mailbox

class UnboundedMailbox {
    companion object {
        fun create (stats : Array<IMailboxStatistics>) : IMailbox {
            return DefaultMailbox(UnboundedMailboxQueue(), UnboundedMailboxQueue(), stats)
        }
    }
}