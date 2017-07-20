package actor.proto.fixture

import actor.proto.mailbox.MailboxStatistics
import java.util.concurrent.CountDownLatch

class TestMailboxStatistics(private val waitForReceived: (Any) -> Boolean) : MailboxStatistics {
    val reset: CountDownLatch = CountDownLatch(1)
    val stats: MutableList<Any> = mutableListOf()
    val posted: MutableList<Any> = mutableListOf()
    val received: MutableList<Any> = mutableListOf()

    override fun mailboxStarted() {
        stats.add("Started")
    }

    override fun messagePosted(message: Any) {
        stats.add(message)
        posted.add(message)
    }

    override fun messageReceived(message: Any) {
        stats.add(message)
        received.add(message)
        if (waitForReceived(message))
            reset.countDown()
    }

    override fun mailboxEmpty() {
        stats.add("Empty")
    }
}

