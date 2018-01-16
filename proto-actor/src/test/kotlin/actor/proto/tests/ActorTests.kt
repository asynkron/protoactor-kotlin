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
    private fun spawnActorFromFunc(receive: suspend Context.(msg:Any) -> Unit): PID = spawn(fromFunc(receive))
    @Test fun requestActorAsync(): Unit {
        val pid: PID = spawnActorFromFunc { msg ->
            when (msg) {
                is String -> respond("hey")
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
        val pid: PID = spawnActorFromFunc { msg ->
            when (msg) {
                is String -> respond("hey")
            }
        }

        runBlocking {
            val reply: Any = requestAwait(pid,"hello", Duration.ofMillis(100))
            assertEquals("hey", reply)
        }
    }

    @Test fun actorLifeCycle(): Unit {
        val messages: Queue<Any> = ArrayDeque<Any>()
        val prop = fromFunc { msg ->
            messages.offer(msg)
        }.withMailbox { TestMailbox() }
        val pid: PID = spawn(prop)
        send(pid,"hello")
        stop(pid)
        assertEquals(4, messages.count())
        val messageArr: Array<Any> = messages.toTypedArray()
        assertTrue(messageArr[0] is Started)
        assertTrue(messageArr[1] is String)
        assertTrue(messageArr[2] is Stopping)
        assertTrue(messageArr[3] is Stopped)
    }

    @Test fun actorStartedException(): Unit {
        val numberExceptions = 2
        var exceptionCount = 0
        val messages: Queue<Any> = ArrayDeque<Any>()
        val prop = fromFunc { msg ->
            messages.offer(msg)
            when (msg) {
                is Started -> {
                    exceptionCount++
                    if (exceptionCount <= 2) throw Exception()
                }
                else -> {

                }
            }
        }
        val pid: PID = spawn(prop)
        send(pid,"hello")
        Thread.sleep(100)
        stop(pid)
        Thread.sleep(100)
        assertEquals(8, messages.count())
        val messageArr: Array<Any> = messages.toTypedArray()
        assertTrue(messageArr[0] is Started)
        assertTrue(messageArr[1] is Restarting)
        assertTrue(messageArr[2] is Started)
        assertTrue(messageArr[3] is Restarting)
        assertTrue(messageArr[4] is Started)
        assertTrue(messageArr[5] is String)
        assertTrue(messageArr[6] is Stopping)
        assertTrue(messageArr[7] is Stopped)
    }
}