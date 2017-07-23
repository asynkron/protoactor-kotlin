package actor.proto.mailbox

interface MailboxQueue {
    val hasMessages: Boolean
    fun push(message: Any)
    fun pop(): Any?
}

