package proto.mailbox

class Dispatchers {
    companion object {
        val defaultDispatcher: ThreadPoolDispatcher = ThreadPoolDispatcher()
    }
}