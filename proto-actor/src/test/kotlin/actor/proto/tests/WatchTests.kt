package actor.proto.tests

import actor.proto.Actor
import actor.proto.Context
import actor.proto.PID
import actor.proto.Started
import actor.proto.Terminated
import actor.proto.fixture.DoNothingActor
import actor.proto.fixture.TestMailbox
import actor.proto.fromProducer
import actor.proto.requestAwait
import actor.proto.spawn
import actor.proto.stop
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration

class WatchTests {
    @Test
    fun `can watch local actors`() {
        runBlocking {
            val watchee: PID = spawn(fromProducer { DoNothingActor() }.withMailbox { TestMailbox() })
            val watcher: PID = spawn(fromProducer { LocalActor(watchee) }.withMailbox { TestMailbox() })
            stop(watchee)
            val terminatedMessageReceived: Boolean = requestAwait(watcher, "?", Duration.ofSeconds(5))
            assertTrue(terminatedMessageReceived)
        }
    }

    class LocalActor(watchee: PID) : Actor {
        private val _watchee: PID = watchee
        private var _terminateReceived: Boolean = false
        override suspend fun Context.receive(msg: Any) {
            when (msg) {
                is Started -> watch(_watchee)
                is String -> respond(_terminateReceived)
                is Terminated -> _terminateReceived = true
            }
        }
    }
}

