package proto.mailbox

object Dispatchers {
    val DEFAULT_DISPATCHER: Dispatcher = ThreadPoolDispatcher()
    val SYNCHRONOUS_DISPATCHER: Dispatcher = SynchronousDispatcher()
}

