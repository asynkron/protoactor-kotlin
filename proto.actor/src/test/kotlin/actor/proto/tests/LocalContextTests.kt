package actor.proto.tests

import actor.proto.Actor
import actor.proto.ActorContext
import actor.proto.Context
import actor.proto.PID
import actor.proto.fixture.DoNothingSupervisorStrategy
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocalContextTests {
    @Test fun `given context ctor should set required fields`() {
        val producer : () -> Actor = { NullActor }
        val supervisorStrategyMock : DoNothingSupervisorStrategy = DoNothingSupervisorStrategy()
        val middleware :  (Context) -> Unit =  { }
        val parent : PID = PID("test", "test")
        val context : ActorContext = ActorContext(producer, supervisorStrategyMock, middleware, null, parent)
        assertEquals(parent, context.parent)
        assertNull(context.sender)
        assertNotNull(context.children)
        assertEquals(context.children, setOf<PID>())
        assertEquals(Duration.ZERO, context.getReceiveTimeout())
    }
}

