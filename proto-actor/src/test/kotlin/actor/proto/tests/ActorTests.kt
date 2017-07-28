package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.EmptyReceive
import actor.proto.fixture.TestMailbox
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.time.Duration
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ActorTests {
    private fun spawnActorFromFunc(receive: suspend (Context) -> Unit): PID = spawn(fromFunc(receive))
    @Test fun requestActorAsync(): Unit {
        val pid: PID = spawnActorFromFunc { ctx ->
            when (ctx.message) {
                is String -> ctx.respond("hey")
            }
        }

        runBlocking {
            val reply: Any = requestAwait(pid,"hello", Duration.ofMillis(200))
            assertEquals("hey", reply)
        }
    }

    @Test fun `request actor async should raise timeout exception when timeout is reached`(): Unit {
        val pid: PID = spawnActorFromFunc(EmptyReceive)
        assertFailsWith<CancellationException> {
            runBlocking {
                requestAwait<Any>(pid,"", Duration.ofMillis(10))
            }
        }
    }

    @Test fun `request actor async should not raise timeout exception when result is first`(): Unit {
        val pid: PID = spawnActorFromFunc { ctx ->
            when (ctx.message) {
                is String -> ctx.respond("hey")
            }
        }

        runBlocking {
            val reply: Any = requestAwait(pid,"hello", Duration.ofMillis(100))
            assertEquals("hey", reply)
        }
    }

    @Test fun actorLifeCycle(): Unit {
        val messages: Queue<Any> = ArrayDeque<Any>()
        val prop = fromFunc {
            messages.offer(message)
        }.withMailbox { TestMailbox() }
        val pid: PID = spawn(prop)
        pid.send("hello")
        stop(pid)
        assertEquals(4, messages.count())
        val messageArr: Array<Any> = messages.toTypedArray()
        assertTrue(messageArr[0] is Started)
        assertTrue(messageArr[1] is String)
        assertTrue(messageArr[2] is Stopping)
        assertTrue(messageArr[3] is Stopped)
    }
}