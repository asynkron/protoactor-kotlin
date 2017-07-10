package proto

import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class EventStreamImpl<T> {
    private val _subscriptions: ConcurrentHashMap<UUID, EventSubscription<T>> = ConcurrentHashMap()
    fun subscribe(action: (T) -> Unit, dispatcher: IDispatcher): EventSubscription<T> {
        val sub: EventSubscription<T> = EventSubscription(this, dispatcher, { x ->
            action(x)
            Actor.Done
        })

        _subscriptions.put(sub.id, sub)
        return sub
    }

    fun publish (msg : T) {
        for(sub in _subscriptions) {
            sub.value.dispatcher.schedule{ ->
                try  {
                    sub.value.action(msg)
                }
                catch (ex : Exception) {
                   // _logger.logWarning(0, ex, "Exception has occurred when publishing a message.")
                }
            }
        }
    }
    internal fun unsubscribe (id : UUID) {
        _subscriptions.remove(id)
    }
}