package actor.proto.tests

import actor.proto.PID
import actor.proto.fromFunc
import actor.proto.requestAwait
import actor.proto.spawn
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class FutureTests {
    @Test fun `given actor when requestAwait should return reply`() {
        val pid: PID = spawn(fromFunc { msg ->
            if (msg is String) {
                respond("hey")
            }
        })
        runBlocking {
            val reply: Any = requestAwait(pid,"hello", Duration.ofMillis(200))
            Assert.assertEquals("hey", reply)
        }
    }

    @Test fun `given actor when await context_requestAwait should get reply`() {
        val pid1: PID = spawn(fromFunc { msg ->
            if (msg is String) {
                respond("hey")
            }
        })
        val pid2: PID = spawn(fromFunc { msg ->
            if (msg is String) {
                val reply1 = requestAwait<String>(pid1, "", Duration.ofMillis(200))
                respond(msg + reply1)
            }
        })
        runBlocking {
            val reply2 = requestAwait<String>(pid2,"hello", Duration.ofMillis(200))
            assertEquals("hellohey", reply2)
        }
    }
}

