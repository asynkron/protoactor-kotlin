package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.DoNothingActor
import actor.proto.fixture.TestMailbox
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class WatchTests {
    @Test fun `can watch local actors`() {
        runBlocking {
            val watchee: PID = spawn(fromProducer { DoNothingActor() }.withMailbox { TestMailbox() })
            val watcher: PID = spawn(fromProducer { LocalActor(watchee) }.withMailbox { TestMailbox() })
            stop(watchee)
            val terminatedMessageReceived: Boolean = requestAwait(watcher,"?", Duration.ofSeconds(5))
            assertTrue(terminatedMessageReceived)
        }
    }

    class LocalActor(watchee: PID) : Actor {
        private val _watchee: PID = watchee
        private var _terminateReceived: Boolean = false
        suspend override fun Context.receive(message : Any) {
            when (message) {
                is Started -> watch(_watchee)
                is String -> respond(_terminateReceived)
                is Terminated -> _terminateReceived = true
            }
        }
    }
}

