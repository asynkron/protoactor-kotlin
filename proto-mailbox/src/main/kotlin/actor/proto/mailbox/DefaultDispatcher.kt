package actor.proto.mailbox

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class DefaultDispatcher( override var throughput: Int = 300) : Dispatcher {
    override fun schedule(mailbox:Mailbox) {

        GlobalScope.launch {
            mailbox.run()
        }
    }
}

