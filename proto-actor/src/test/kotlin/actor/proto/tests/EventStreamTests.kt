package actor.proto.tests

import actor.proto.EventStreamImpl
import actor.proto.mailbox.Dispatchers
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class EventStreamTests {
    @Test
    fun `event stream can subscribe to specific event types`() {
        var received = ""
        val eventStream = EventStreamImpl()
        eventStream.subscribe({ theString -> received = theString as String })
        eventStream.publish("hello")
        assertEquals("hello", received)

    }

    @Test
    fun `eventStream can subscribe to all event types`() {
        val receivedEvents = mutableListOf<Any>()
        val eventStream = EventStreamImpl()
        eventStream.subscribe({ msg -> receivedEvents.add(msg) })
        eventStream.publish("hello")
        eventStream.publish(1)
        eventStream.publish(true)
        assertEquals(3, receivedEvents.count())
    }

    @Test
    fun `eventStream can unsubscribe from events`() {
        val receivedEvents = mutableListOf<Any>()
        val eventStream = EventStreamImpl()
        val subscription = eventStream.subscribe({ msg -> receivedEvents.add(msg) })
        eventStream.publish("first message")
        subscription.unsubscribe()
        eventStream.publish("second message")
        assertEquals(1, receivedEvents.count())
    }

    //    @Test fun eventStream_OnlyReceiveSubscribedToEventTypes () {
//        val eventsReceived = mutableListOf<Any>()
//        val eventStream = EventStreamImpl()
//        eventStream.subscribe({ eventsReceived.add(it)})
//        eventStream.publish("not an int")
//        Assert.assertEquals(0, eventsReceived.count())
//    }
    @Test
    fun `event stream can subscribe to specific event types async`() {
        val eventStream = EventStreamImpl()
        val cd = CountDownLatch(1)
        eventStream.subscribe({ cd.countDown() }, Dispatchers.DEFAULT_DISPATCHER)
        eventStream.publish("hello")
        cd.await(1000, TimeUnit.MILLISECONDS)
    }
}

