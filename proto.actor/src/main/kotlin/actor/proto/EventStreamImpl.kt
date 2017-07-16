package actor.proto

import actor.proto.mailbox.Dispatcher
import actor.proto.mailbox.Dispatchers
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class EventStreamImpl {
    private val subscriptions: ConcurrentHashMap<UUID, EventSubscription> = ConcurrentHashMap()
    fun subscribe(action: (Any) -> Unit, dispatcher: Dispatcher = Dispatchers.SYNCHRONOUS_DISPATCHER): EventSubscription {
        val sub = EventSubscription(this, dispatcher, action)
        subscriptions.put(sub.id, sub)
        return sub
    }


    fun publish(msg: Any) {
        for (sub in subscriptions) {
            sub.value.dispatcher.schedule { ->
                try {
                    sub.value.action(msg)
                } catch (ex: Exception) {
                    // _logger.logWarning(0, ex, "Exception has occurred when publishing a message.")
                }
            }
        }
    }

    internal fun unsubscribe(id: UUID) {
        subscriptions.remove(id)
    }
}