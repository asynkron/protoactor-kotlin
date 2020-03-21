package actor.proto.tests

import actor.proto.Actor
import actor.proto.ActorContext
import actor.proto.PID
import actor.proto.fixture.DoNothingSupervisorStrategy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

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
        assertEquals(context.children, setOf<PID>())
        assertEquals(Duration.ZERO, context.getReceiveTimeout())
    }
}

