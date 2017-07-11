package proto.mailbox

interface Dispatcher {
    val throughput: Int
    fun schedule(runner: suspend () -> Unit)
}