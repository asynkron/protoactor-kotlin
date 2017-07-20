package actor.proto.fixture

import actor.proto.mailbox.Dispatcher

class TestDispatcher : Dispatcher {
    override var throughput: Int = 10
    override fun schedule(runner: suspend () -> Unit) {
    }
}

