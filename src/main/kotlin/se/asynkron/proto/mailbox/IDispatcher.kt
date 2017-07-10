package proto.mailbox

interface IDispatcher {
    val throughput: Int
    fun schedule(runner: suspend () -> Unit)
}