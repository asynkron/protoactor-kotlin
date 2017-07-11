package proto.mailbox

interface MailboxStatistics {
    fun mailboxStarted()
    fun messagePosted(message: Any)
    fun messageReceived(message: Any)
    fun mailboxEmpty()
}