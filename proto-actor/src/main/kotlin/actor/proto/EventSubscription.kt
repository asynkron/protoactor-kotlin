package actor.proto

import actor.proto.mailbox.Dispatcher
import java.util.*

class EventSubscription(private val eventStream: EventStreamImpl, val dispatcher: Dispatcher, val action: (Any) -> Unit) {
    val id: UUID = UUID.randomUUID()
    fun unsubscribe() {
        eventStream.unsubscribe(id)
    }
}