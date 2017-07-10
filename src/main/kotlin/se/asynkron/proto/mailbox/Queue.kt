package proto.mailbox

interface IMailboxQueue {
    val hasMessages: Boolean
    fun push(message: Any)
    fun pop(): Any?
}

