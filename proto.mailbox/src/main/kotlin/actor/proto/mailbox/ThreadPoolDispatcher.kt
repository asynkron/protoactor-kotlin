package actor.proto.mailbox

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.launch


class ThreadPoolDispatcher(override var throughput: Int = 300) : Dispatcher {
    override fun schedule(runner: suspend CoroutineScope.() -> Unit) {
        launch(CommonPool, CoroutineStart.DEFAULT, runner)
    }
}

