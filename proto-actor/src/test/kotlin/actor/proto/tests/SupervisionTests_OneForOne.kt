package actor.proto.tests

import actor.proto.*
import actor.proto.fixture.TestMailboxStatistics
import actor.proto.mailbox.ResumeMailbox
import actor.proto.mailbox.newUnboundedMailbox
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SupervisionTests_OneForOne {
    companion object {
        private val Exception: Exception = Exception("boo hoo")
    }

    class ParentActor(childProps: Props) : Actor {
        private val _childProps: Props = childProps
        lateinit var child: PID

        override suspend fun Context.receive(msg: Any) {
            if (msg is Started)
                child = spawnChild(_childProps)

            if (msg is String)
                send(child, msg)
        }
    }

    class ChildActor : Actor {
        override suspend fun Context.receive(msg: Any) {
            val tmp = msg
            when (tmp) {
                is String -> {
                    throw Exception
                }
            }

        }
    }

    class ThrowOnStartedChildActor : Actor {
        override suspend fun Context.receive(msg: Any) {
            val tmp = msg
            when (tmp) {
                is Started -> {
                    throw Exception("in started")
                }
            }
        }
    }

    @Test
    fun `Should resume child on failure`() {
        val childMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is ResumeMailbox }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Resume }, 1, null)
        val childProps: Props = fromProducer { ChildActor() }.withMailbox { newUnboundedMailbox(arrayOf(childMailboxStats)) }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy)
        val parent: PID = spawn(parentProps)

        send(parent, "hello")
        childMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        assertTrue { childMailboxStats.posted.contains(ResumeMailbox) }
        assertTrue { childMailboxStats.received.contains(ResumeMailbox) }
    }

    @Test
    fun `Should stop child on failure`() {
        val childMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Stop }, 1, null)
        val childProps: Props = fromProducer { ChildActor() }.withMailbox { newUnboundedMailbox(arrayOf(childMailboxStats)) }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy)
        val parent: PID = spawn(parentProps)

        send(parent, "hello")
        childMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        assertTrue { childMailboxStats.posted.contains(StopInstance) }
        assertTrue { childMailboxStats.received.contains(StopInstance) }
    }

    @Test
    fun `Should restart child on failure`() {
        val childMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Restart }, 1, null)
        val childProps: Props = fromProducer { ChildActor() }.withMailbox { newUnboundedMailbox(arrayOf(childMailboxStats)) }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy)
        val parent: PID = spawn(parentProps)

        send(parent, "hello")
        childMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        assertTrue { childMailboxStats.posted.any { it is Restart } }
        assertTrue { childMailboxStats.received.any { it is Restart } }
    }

    @Test
    fun `Should pass exception on restart`() {
        val childMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Restart }, 1, null)
        val childProps: Props = fromProducer { ChildActor() }.withMailbox { newUnboundedMailbox(arrayOf(childMailboxStats)) }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy)
        val parent: PID = spawn(parentProps)

        send(parent, "hello")
        childMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        assertTrue { childMailboxStats.posted.any { it is Restart && it.reason == Exception } }
        assertTrue { childMailboxStats.received.any { it is Restart && it.reason == Exception } }
    }

    @Test
    fun `Should stop child when restart limit has been reached`() {
        val childMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Restart }, 1, null)
        val childProps: Props = fromProducer { ChildActor() }.withMailbox { newUnboundedMailbox(arrayOf(childMailboxStats)) }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy)
        val parent: PID = spawn(parentProps)

        send(parent, "hello")
        send(parent, "hello")
        childMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        assertTrue { childMailboxStats.posted.contains(StopInstance) }
        assertTrue { childMailboxStats.received.contains(StopInstance) }
    }

    @Test
    fun `Should revert to default directive when escalate directive without grand parent`() {
        val parentMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Escalate }, 1, null)
        val childProps: Props = fromProducer { ThrowOnStartedChildActor() }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy).withMailbox { newUnboundedMailbox(arrayOf(parentMailboxStats)) }
        val parent: PID = spawn(parentProps)
        send(parent, "hello")

        parentMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        val filterIsInstance = parentMailboxStats.received.filterIsInstance<Failure>()
        assertEquals(11, filterIsInstance.size)
        filterIsInstance.forEach { failure ->
            assertEquals("in started", failure.reason.message)
        }
    }

    @Test
    fun `Should escalate failure to parent`() {
        val parentMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Escalate }, 1, null)
        val childProps: Props = fromProducer { ChildActor() }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy).withMailbox { newUnboundedMailbox(arrayOf(parentMailboxStats)) }
        val parent: PID = spawn(parentProps)

        send(parent, "hello")
        parentMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        val failure = parentMailboxStats.received.filterIsInstance<Failure>().single()
        assertEquals("boo hoo", failure.reason.message)
    }

    @Test
    fun `Should stop child on failure when started`() {
        val childMailboxStats: TestMailboxStatistics = TestMailboxStatistics { it is Stopped }
        val strategy: OneForOneStrategy = OneForOneStrategy({ _, _ -> SupervisorDirective.Stop }, 1, null)
        val childProps: Props = fromProducer { ThrowOnStartedChildActor() }.withMailbox { newUnboundedMailbox(arrayOf(childMailboxStats)) }
        val parentProps: Props = fromProducer { ParentActor(childProps) }.withChildSupervisorStrategy(strategy)

        spawn(parentProps)
        childMailboxStats.reset.await(1000L, TimeUnit.MILLISECONDS)

        assertTrue { childMailboxStats.posted.contains(StopInstance) }
        assertTrue { childMailboxStats.received.contains(StopInstance) }
    }
}
