package actor.proto.mailbox

import kotlinx.coroutines.experimental.CommonPool
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.startCoroutine


class DefaultDispatcher(coroutineContext: CoroutineContext = CommonPool, override var throughput: Int = 300) : Dispatcher {
    private val f = EmptyContinuation(coroutineContext)
    override fun schedule(mailbox:Mailbox) {
        val runner: suspend () -> Unit = {mailbox.run()}
        runner.startCoroutine(f)
    }
}

class EmptyContinuation(override val context: CoroutineContext) : Continuation<Unit> {
    override fun resume(value: Unit) {
    }
    override fun resumeWithException(exception: Throwable) {
    }
}