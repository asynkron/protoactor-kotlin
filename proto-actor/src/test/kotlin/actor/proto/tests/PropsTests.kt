package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.DoNothingSupervisorStrategy
import actor.proto.fixture.TestDispatcher
import actor.proto.fixture.TestMailbox
import actor.proto.mailbox.Mailbox
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

object NullActor : Actor {
    suspend override fun receive(context: Context) {
    }
}

class PropsTests {
    @Test fun `given props when withDispatcher then mutate dispatcher`() {
        val dispatcher: TestDispatcher = TestDispatcher()
        val props: Props = Props()
        val props2: Props = props.withDispatcher(dispatcher)
        assertNotEquals(props, props2)
        assertEquals(dispatcher, props2.dispatcher)
        assertNotEquals(props.dispatcher, props2.dispatcher)
        assertEquals(props.mailboxProducer, props2.mailboxProducer)
        assertEquals(props.receiveMiddleware, props2.receiveMiddleware)
        assertEquals(props.producer, props2.producer)
        assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }

    @Test fun `given props when withMailbox then mutate mailboxProducer`() {
        val mailboxProducer: () -> Mailbox = { TestMailbox() }
        val props: Props = Props()
        val props2: Props = props.withMailbox(mailboxProducer)
        assertNotEquals(props, props2)
        assertEquals(mailboxProducer, props2.mailboxProducer)
        assertEquals(props.dispatcher, props2.dispatcher)
        assertNotEquals(props.mailboxProducer, props2.mailboxProducer)
        assertEquals(props.receiveMiddleware, props2.receiveMiddleware)
        assertEquals(props.producer, props2.producer)
        assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }

    //    fun given_Props_When_WithMiddleware_Then_mutate_Middleware () {
//        val middleware : (suspend (Context) -> Unit) -> suspend (Context) -> Unit = { r -> r}
//        val middleware2 : (suspend (Context) -> Unit) -> suspend (Context) -> Unit = {r -> r}
//        val middleware3 : (suspend (Context) -> Unit) -> suspend (Context) -> Unit = {r -> r}
//        val props : Props = Props()
//        val props2 : Props = props.withReceiveMiddleware(middleware, middleware2)
//        val props3 : Props = props2.withReceiveMiddleware(middleware3)
//        Assert.assertNotEquals(props, props2)
//        Assert.assertEquals(props.dispatcher, props2.dispatcher)
//        Assert.assertEquals(props.mailboxProducer, props2.mailboxProducer)
//        Assert.assertNotEquals(props.receiveMiddlewareChain, props2.receiveMiddlewareChain)
//        Assert.assertEquals(props.producer, props2.producer)
//        Assert.assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
//    }
    @Test fun `given props when withProducer then mutate producer`() {
        val producer: () -> Actor = { NullActor }
        val props: Props = Props()
        val props2: Props = props.withProducer(producer)
        assertNotEquals(props, props2)
        assertEquals(producer, props2.producer)
        assertEquals(props.dispatcher, props2.dispatcher)
        assertEquals(props.mailboxProducer, props2.mailboxProducer)
        assertEquals(props.receiveMiddleware, props2.receiveMiddleware)
        assertNotEquals(props.producer, props2.producer)
        assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }

    @Test fun `given props when withSpawner then mutate spawner`() {
        val spawner: (String, Props, PID?) -> PID = { _, _, _ -> PID("abc", "def") }
        val props: Props = Props()
        val props2: Props = props.withSpawner(spawner)
        assertNotEquals(props, props2)
        assertEquals(props.dispatcher, props2.dispatcher)
        assertEquals(props.mailboxProducer, props2.mailboxProducer)
        assertEquals(props.receiveMiddleware, props2.receiveMiddleware)
        assertEquals(props.producer, props2.producer)
        assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }

    @Test fun `given props when withSupervisor then mutate supervisorStrategy`() {
        val supervision: DoNothingSupervisorStrategy = DoNothingSupervisorStrategy()
        val props: Props = Props()
        val props2: Props = props.withChildSupervisorStrategy(supervision)
        assertNotEquals(props, props2)
        assertEquals(supervision, props2.supervisorStrategy)
        assertEquals(props.dispatcher, props2.dispatcher)
        assertEquals(props.mailboxProducer, props2.mailboxProducer)
        assertEquals(props.receiveMiddleware, props2.receiveMiddleware)
        assertEquals(props.producer, props2.producer)
        assertNotEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }
}

