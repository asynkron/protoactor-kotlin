package actor.proto.mailbox

object Dispatchers {
    val DEFAULT_DISPATCHER: Dispatcher = DefaultDispatcher()
    val SYNCHRONOUS_DISPATCHER: Dispatcher = SynchronousDispatcher()
}

