package proto.mailbox

object Dispatchers {
    val defaultDispatcher: IDispatcher = ThreadPoolDispatcher()
    val synchronousDispatcher: IDispatcher = SynchronousDispatcher()
}

