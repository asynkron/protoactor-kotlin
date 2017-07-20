package actor.proto.tests

import actor.proto.*
import actor.proto.mailbox.Dispatchers
import org.junit.Assert

open class EventStreamTests {
    fun eventStream_CanSubscribeToSpecificEventTypes () {
        var received : String = ""
        val eventStream = EventStreamImpl()
        eventStream.subscribe({theString -> received = theString as String})
        eventStream.publish("hello")
        Assert.assertEquals("hello", received)
    }
    fun eventStream_CanSubscribeToAllEventTypes () {
        val receivedEvents = mutableListOf<Any>()
        val eventStream = EventStreamImpl()
        eventStream.subscribe({msg -> receivedEvents.add(msg)})
        eventStream.publish("hello")
        eventStream.publish(1)
        eventStream.publish(true)
        Assert.assertEquals(3, receivedEvents.count())
    }
    fun eventStream_CanUnsubscribeFromEvents () {
        val receivedEvents = mutableListOf<Any>()
        val eventStream = EventStreamImpl()
        val subscription  = eventStream.subscribe({msg -> receivedEvents.add(msg)})
        eventStream.publish("first message")
        subscription.unsubscribe()
        eventStream.publish("second message")
        Assert.assertEquals(1, receivedEvents.count())
    }
    fun eventStream_OnlyReceiveSubscribedToEventTypes () {
        val eventsReceived = mutableListOf<Any>()
        val eventStream = EventStreamImpl()
        eventStream.subscribe({msg -> eventsReceived.add(msg)})
        eventStream.publish("not an int")
        Assert.assertEquals(0, eventsReceived.count())
    }
    fun eventStream_CanSubscribeToSpecificEventTypes_Async () {
        var received : String = ""
        val eventStream = EventStreamImpl()
        eventStream.subscribe({theString ->
            received = theString as String
            Assert.assertEquals("hello", received)
        }, Dispatchers.DEFAULT_DISPATCHER)
        eventStream.publish("hello")
    }
}

