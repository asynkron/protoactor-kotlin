package actor.proto.tests

import actor.proto.Actor
import actor.proto.ActorContext
import actor.proto.PID
import actor.proto.fixture.DoNothingSupervisorStrategy
import org.junit.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocalContextTests {
    @Test
    fun `given context ctor should set required fields`() {
        val producer: () -> Actor = { NullActor }
        val supervisorStrategyMock = DoNothingSupervisorStrategy()
        val parent = PID("test", "test")
        val self = PID("abc", "def")
        val context = ActorContext(producer, self, supervisorStrategyMock, listOf(), listOf(), parent)
        assertEquals(parent, context.parent)
        assertNull(context.sender)
        assertNotNull(context.children)
        assertEquals(context.children, setOf())
        assertEquals(Duration.ZERO, context.getReceiveTimeout())
    }
}

