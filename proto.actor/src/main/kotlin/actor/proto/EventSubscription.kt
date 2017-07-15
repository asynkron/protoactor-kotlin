package actor.proto

import actor.proto.mailbox.Dispatcher
import java.util.*

class EventSubscription<T>(private val eventStream: EventStreamImpl<T>, val dispatcher: Dispatcher, val action: (T) -> Unit) {
    val id = UUID.randomUUID()
    fun unsubscribe() {
        eventStream.unsubscribe(id)
    }
}