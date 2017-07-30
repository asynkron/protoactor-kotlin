package actor.proto.fixture

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Mailbox

class TestDispatcher : Dispatcher {
    override var throughput: Int = 10
    override fun schedule(mailbox:Mailbox) {
    }
}

