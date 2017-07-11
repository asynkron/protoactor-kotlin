package proto.mailbox

interface IMailboxStatistics {
    fun mailboxStarted()
    fun messagePosted(message: Any)
    fun messageReceived(message: Any)
    fun mailboxEmpty()
}