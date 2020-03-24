package actor.proto.router.tests

import actor.proto.Actor
import actor.proto.Context
import actor.proto.PID
import actor.proto.Props
import actor.proto.fromProducer
import actor.proto.requestAwait
import actor.proto.router.Routees
import actor.proto.router.RouterAddRoutee
import actor.proto.router.RouterBroadcastMessage
import actor.proto.router.RouterGetRoutees
import actor.proto.router.RouterRemoveRoutee
import actor.proto.router.fixture.TestMailbox
import actor.proto.router.newBroadcastGroup
import actor.proto.send
import actor.proto.spawn
import actor.proto.stop
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class BroadcastGroupTests {
    private val _props: Props = fromProducer { MyTestActor() }
    private val _timeout: Duration = Duration.ofMillis(1000)

    @Test
    fun `broadcast group router, all routees receive messages`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            send(router,"hello")
            assertEquals("hello", requestAwait(routee1,"received?", _timeout))
            assertEquals("hello", requestAwait(routee2,"received?", _timeout))
            assertEquals("hello", requestAwait(routee3,"received?", _timeout))
        }
    }

    @Test fun `broadcast group router, when one routee is stopped, all other routees receive messages`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            stop(routee2)
            send(router,"hello")
            assertEquals("hello", requestAwait(routee1,"received?", _timeout))
            assertEquals("hello", requestAwait(routee3,"received?", _timeout))
        }
    }

    @Test fun `broadcast group router, when one routee is slow, all other routees receive messages`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            send(routee2,"go slow")
            send(router,"hello")
            assertEquals("hello", requestAwait(routee1,"received?", _timeout))
            assertEquals("hello", requestAwait(routee3,"received?", _timeout))
        }
    }

    @Test fun `broadcast group router, routees can be removed`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            send(router,RouterRemoveRoutee(routee1))
            val routees = requestAwait<Routees>(router,RouterGetRoutees, _timeout)
            assertFalse(routees.pids.contains(routee1))
            assertTrue(routees.pids.contains(routee2))
            assertTrue(routees.pids.contains(routee3))
        }
    }

    @Test fun `broadcast group router, routees can be added`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            val routee4 = spawn(_props)
            send(router,RouterAddRoutee(routee4))
            val routees = requestAwait<Routees>(router,RouterGetRoutees, _timeout)
            assertTrue(routees.pids.contains(routee1))
            assertTrue(routees.pids.contains(routee2))
            assertTrue(routees.pids.contains(routee3))
            assertTrue(routees.pids.contains(routee4))
        }
    }

    @Test fun `broadcast group router, removed routees no longer receive messages`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            send(router,"first message")
            send(router,RouterRemoveRoutee(routee1))
            send(router,"second message")
            assertEquals("first message", requestAwait(routee1,"received?", _timeout))
            assertEquals("second message", requestAwait(routee2,"received?", _timeout))
            assertEquals("second message", requestAwait(routee3,"received?", _timeout))
        }
    }

    @Test fun `broadcast group router, added routees receive messages`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            val routee4 = spawn(_props)
            send(router,RouterAddRoutee(routee4))
            send(router,"a message")
            assertEquals("a message", requestAwait(routee1,"received?", _timeout))
            assertEquals("a message", requestAwait(routee2,"received?", _timeout))
            assertEquals("a message", requestAwait(routee3,"received?", _timeout))
            assertEquals("a message", requestAwait(routee4,"received?", _timeout))
        }
    }

    @Test fun `broadcast group router, all routees receive router broadcast messages`() {
        runBlocking {
            val (router, routee1, routee2, routee3) = createBroadcastGroupRouterWith3Routees()
            send(router,RouterBroadcastMessage("hello"))
            delay(100)
            assertEquals("hello", requestAwait(routee1,"received?", _timeout))
            assertEquals("hello", requestAwait(routee2,"received?", _timeout))
            assertEquals("hello", requestAwait(routee3,"received?", _timeout))
        }
    }

    private fun createBroadcastGroupRouterWith3Routees(): Array<PID> {
        val routee1 = spawn(_props)
        val routee2 = spawn(_props)
        val routee3 = spawn(_props)
        val props = newBroadcastGroup(setOf(routee1, routee2, routee3)).withMailbox { TestMailbox() }
        val router = spawn(props)
        return arrayOf(router, routee1, routee2, routee3)
    }

    internal open class MyTestActor : Actor {
        private lateinit var _received: String
        override suspend fun Context.receive(msg: Any) {
            val tmp = msg
            when (tmp) {
                "received?" -> respond(_received)
                "go slow" -> delay(5000)
                is String -> _received = tmp
            }
        }
    }
}
