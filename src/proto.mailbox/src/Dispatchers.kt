package proto.mailbox

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Dispatchers {
    companion object {
        val defaultDispatcher: ThreadPoolDispatcher = ThreadPoolDispatcher()
    }
}
