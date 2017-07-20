package proto.tests

import actor.proto.Actor
import actor.proto.Context
import actor.proto.PID
import actor.proto.Props
import actor.proto.fixture.DoNothingSupervisorStrategy
import actor.proto.fixture.TestDispatcher
import actor.proto.fixture.TestMailbox
import actor.proto.mailbox.Mailbox
import org.junit.Assert
import org.junit.Test

object NullActor : Actor{
    suspend override fun receive(context: Context) {
    }
}
class PropsTests {
    @Test fun given_Props_When_WithDispatcher_Then_mutate_Dispatcher () {
        val dispatcher : TestDispatcher = TestDispatcher()
        val props : Props = Props()
        val props2 : Props = props.withDispatcher(dispatcher)
        Assert.assertNotEquals(props, props2)
        Assert.assertEquals(dispatcher, props2.dispatcher)
        Assert.assertNotEquals(props.dispatcher, props2.dispatcher)
        Assert.assertEquals(props.mailboxProducer, props2.mailboxProducer)
        Assert.assertEquals(props.receiveMiddlewareChain, props2.receiveMiddlewareChain)
        Assert.assertEquals(props.producer, props2.producer)
        Assert.assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }
    @Test fun given_Props_When_WithMailbox_Then_mutate_MailboxProducer () {
        val mailboxProducer : () -> Mailbox = { TestMailbox() }
        val props : Props = Props()
        val props2 : Props = props.withMailbox(mailboxProducer)
        Assert.assertNotEquals(props, props2)
        Assert.assertEquals(mailboxProducer, props2.mailboxProducer)
        Assert.assertEquals(props.dispatcher, props2.dispatcher)
        Assert.assertNotEquals(props.mailboxProducer, props2.mailboxProducer)
        Assert.assertEquals(props.receiveMiddlewareChain, props2.receiveMiddlewareChain)
        Assert.assertEquals(props.producer, props2.producer)
        Assert.assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
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
@Test fun given_Props_When_WithProducer_Then_mutate_Producer () {
        val producer : () -> Actor = { NullActor }
        val props : Props = Props()
        val props2 : Props = props.withProducer(producer)
        Assert.assertNotEquals(props, props2)
        Assert.assertEquals(producer, props2.producer)
        Assert.assertEquals(props.dispatcher, props2.dispatcher)
        Assert.assertEquals(props.mailboxProducer, props2.mailboxProducer)
        Assert.assertEquals(props.receiveMiddlewareChain, props2.receiveMiddlewareChain)
        Assert.assertNotEquals(props.producer, props2.producer)
        Assert.assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }
    @Test fun given_Props_When_WithSpawner_Then_mutate_Spawner () {
        val spawner : (String, Props, PID?) -> PID = { _, _, _ -> PID("abc","def")}
        val props : Props = Props()
        val props2 : Props = props.withSpawner(spawner)
        Assert.assertNotEquals(props, props2)
        Assert.assertEquals(props.dispatcher, props2.dispatcher)
        Assert.assertEquals(props.mailboxProducer, props2.mailboxProducer)
        Assert.assertEquals(props.receiveMiddlewareChain, props2.receiveMiddlewareChain)
        Assert.assertEquals(props.producer, props2.producer)
        Assert.assertEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }
    @Test fun given_Props_When_WithSupervisor_Then_mutate_SupervisorStrategy () {
        val supervision : DoNothingSupervisorStrategy = DoNothingSupervisorStrategy()
        val props : Props = Props()
        val props2 : Props = props.withChildSupervisorStrategy(supervision)
        Assert.assertNotEquals(props, props2)
        Assert.assertEquals(supervision, props2.supervisorStrategy)
        Assert.assertEquals(props.dispatcher, props2.dispatcher)
        Assert.assertEquals(props.mailboxProducer, props2.mailboxProducer)
        Assert.assertEquals(props.receiveMiddlewareChain, props2.receiveMiddlewareChain)
        Assert.assertEquals(props.producer, props2.producer)
        Assert.assertNotEquals(props.supervisorStrategy, props2.supervisorStrategy)
    }
}

