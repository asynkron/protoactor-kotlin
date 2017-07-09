package proto.mailbox

import java.util.concurrent.ConcurrentLinkedQueue

open class UnboundedMailboxQueue : IMailboxQueue {
    private val _messages: ConcurrentLinkedQueue<Any> = ConcurrentLinkedQueue()
    override fun push(message: Any) {
        _messages.add(message)
    }

    override fun pop(): Any? {
        return _messages.poll()
    }

    override val hasMessages: Boolean
        get() = _messages.count() > 0
}

