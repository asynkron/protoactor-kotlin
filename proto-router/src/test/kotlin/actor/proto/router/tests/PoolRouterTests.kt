package actor.proto.router.tests

import actor.proto.Props
import actor.proto.router.fixture.DoNothingActor
import actor.proto.router.fixture.TestMailbox
import actor.proto.fromProducer
import actor.proto.requestAwait
import actor.proto.router.*
import actor.proto.spawn
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals

class PoolRouterTests {
    private val MyActorProps: Props = fromProducer { DoNothingActor() }
    private val _timeout: Duration = Duration.ofMillis(1000)
    @Test fun broadcastGroupPool_CreatesRoutees() {
        runBlocking {
            val props = newBroadcastPool(MyActorProps, 3).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = router.requestAwait<Routees>(RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }

    @Test fun roundRobinPool_CreatesRoutees() {
        runBlocking {
            val props = newRoundRobinPool(MyActorProps, 3).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = router.requestAwait<Routees>(RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }

    @Test fun consistentHashPool_CreatesRoutees() {
        runBlocking {
            val props = newConsistentHashPool(MyActorProps, 3, { 0 }, 1).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = router.requestAwait<Routees>(RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }

    @Test fun randomPool_CreatesRoutees() {
        runBlocking {
            val props = newRandomPool(MyActorProps, 3, 0).withMailbox { TestMailbox() }
            val router = spawn(props)
            val routees = router.requestAwait<Routees>(RouterGetRoutees, _timeout)
            assertEquals(3, routees.pids.count())
        }
    }
}

