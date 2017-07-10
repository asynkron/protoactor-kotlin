package proto.mailbox

class Dispatchers {
    companion object {
        val defaultDispatcher: IDispatcher = ThreadPoolDispatcher()
        val synchronousDispatcher: IDispatcher = SynchronousDispatcher()
    }
}

