package proto.mailbox

import java.time.Duration
import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock

interface IMailboxStatistics {
    fun mailboxStarted()
    fun messagePosted(message: Any)
    fun messageReceived(message: Any)
    fun mailboxEmpty()
}