package actor.proto.mailbox

import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext


class DefaultDispatcher(context: CoroutineContext = Dispatchers.Default, override var throughput: Int = 300) : Dispatcher {
    // We could create a jobless GlobalScope root scope and replace the default dispatcher with our dispatcher
    // val scope : CoroutineScope = GlobalScope + context
    //Or we can create a scope from our dispatcher with a default job, and replace it with a supervisor job.
    //This way we get a normal scope that could be cancelled if needed
    private val scope : CoroutineScope = CoroutineScope(context) + SupervisorJob()

    override fun schedule(mailbox:Mailbox) {
        scope.launch {
            mailbox.run()
        }
    }
}

