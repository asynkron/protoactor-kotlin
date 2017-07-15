package proto.actor

import proto.mailbox.Dispatcher
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class EventStreamImpl<T> {
    private val subscriptions: ConcurrentHashMap<UUID, EventSubscription<T>> = ConcurrentHashMap()
    fun subscribe(action: (T) -> Unit, dispatcher: Dispatcher): EventSubscription<T> {
        val sub = EventSubscription(this, dispatcher, action)
        subscriptions.put(sub.id, sub)
        return sub
    }

    fun publish(msg: T) {
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