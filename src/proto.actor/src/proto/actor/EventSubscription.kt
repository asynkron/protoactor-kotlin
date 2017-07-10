package proto.actor

import proto.mailbox.IDispatcher
import java.util.*

class EventSubscription<T>(val eventStream: EventStreamImpl<T>, val dispatcher: IDispatcher, val action: (T) -> Task) {
    val id : UUID = UUID.randomUUID()
    fun unsubscribe () {
        eventStream.unsubscribe(id)
    }
}