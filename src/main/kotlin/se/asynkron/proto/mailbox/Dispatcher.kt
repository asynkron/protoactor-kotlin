package proto.mailbox

interface Dispatcher {
    var throughput: Int
    fun schedule(runner: suspend () -> Unit)
}