package proto.mailbox

import kotlinx.coroutines.experimental.runBlocking

class SynchronousDispatcher : IDispatcher {
    override fun schedule(runner: suspend () -> Unit) {
        runBlocking { runner() }
    }

    override var throughput: Int = 300
}