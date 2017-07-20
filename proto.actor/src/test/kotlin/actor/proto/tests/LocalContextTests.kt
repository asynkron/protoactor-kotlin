package proto.tests

import actor.proto.Actor
import actor.proto.ActorContext
import actor.proto.Context
import actor.proto.PID
import actor.proto.fixture.DoNothingSupervisorStrategy
import org.junit.Assert
import org.junit.Test
import java.time.Duration

open class LocalContextTests {
    @Test fun given_Context_ctor_should_set_some_fields () {
        val producer : () -> Actor = {NullActor}
        val supervisorStrategyMock : DoNothingSupervisorStrategy = DoNothingSupervisorStrategy()
        val middleware :  (Context) -> Unit =  { }
        val parent : PID = PID("test", "test")
        val context : ActorContext = ActorContext(producer, supervisorStrategyMock, middleware, null, parent)
        Assert.assertEquals(parent, context.parent)
        Assert.assertNull(context.sender)
        Assert.assertNotNull(context.children)
        Assert.assertEquals(context.children, setOf<PID>())
        Assert.assertEquals(Duration.ZERO, context.getReceiveTimeout())
    }
}

