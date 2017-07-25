package actor.proto.mailbox

import kotlinx.coroutines.experimental.CoroutineScope

interface Dispatcher {
    var throughput: Int
    fun schedule(runner: suspend () -> Unit)
}