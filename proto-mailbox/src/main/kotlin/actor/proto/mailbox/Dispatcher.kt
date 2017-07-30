package actor.proto.mailbox

interface Dispatcher {
    var throughput: Int
    fun schedule(mailbox: Mailbox)
}