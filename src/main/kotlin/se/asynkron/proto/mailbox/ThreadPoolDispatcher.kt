package proto.mailbox

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


class ThreadPoolDispatcher : IDispatcher {
     override fun schedule(runner: suspend () -> Unit) {
        launch(CommonPool){
            runner()
        }
    }
    override var throughput: Int = 300
}

