package proto.tests

import actor.proto.*
import actor.proto.fixture.DoNothingActor
import actor.proto.fixture.TestMailbox
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.time.Duration

class WatchTests {
    @Test fun canWatchLocalActors () {
        runBlocking {
            val watchee: PID = spawn(fromProducer { DoNothingActor() }.withMailbox { TestMailbox() })
            val watcher: PID = spawn(fromProducer { LocalActor(watchee) }.withMailbox { TestMailbox() })
            watchee.stop()
            val terminatedMessageReceived: Boolean = watcher.requestAwait<Boolean>("?", Duration.ofSeconds(5))
            Assert.assertTrue(terminatedMessageReceived)
        }
    }

    class LocalActor(watchee: PID) : Actor {
        private val _watchee : PID = watchee
        private var _terminateReceived : Boolean = false
        suspend override fun receive (context : Context) {
            when (context.message) {
                is Started -> context.watch(_watchee)
                is String -> context.respond(_terminateReceived)
                is Terminated -> _terminateReceived = true
            }
        }
    }
}

