package actor.proto.mailbox

import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext

class EmptyContinuation(override val context: CoroutineContext) : Continuation<Unit> {
    override fun resume(value: Unit) {
    }
    override fun resumeWithException(exception: Throwable) {
    }
}