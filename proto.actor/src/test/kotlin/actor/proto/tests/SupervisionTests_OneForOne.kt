//package proto.tests
//
//import actor.proto.*
//import actor.proto.fixture.TestMailboxStatistics
//import actor.proto.mailbox.ResumeMailbox
//import org.junit.Assert
//
//class SupervisionTests_OneForOne {
//    companion object {
//        private val Exception : Exception = Exception("boo hoo")
//    }
//
//     class ParentActor(childProps: Props) : Actor {
//         private val _childProps: Props = childProps
//         lateinit var child: PID
//         suspend override fun receive(context: Context) {
//             if (context.message is Started)
//                 child = context.spawnChild(_childProps)
//
//             if (context.message is)
//                 child.send(context.message)
//
//
//         }
//     }
//
//    open class ChildActor : Actor {
//        suspend override fun receive (context : Context) {
//            val tmp = context.message
//            when (tmp) {
//                is String -> {
//                    throw Exception
//                }
//            }
//
//        }
//    }
//
//    open class ThrowOnStartedChildActor : Actor {
//        suspend override fun receive (context : Context) {
//            val tmp = context.message
//            when (tmp) {
//                is Started -> {
//                    throw Exception("in started")
//                }
//            }
//
//        }
//    }
//    fun oneForOneStrategy_Should_ResumeChildOnFailure () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is ResumeMailbox }
//        val strategy : OneForOneStrategy = OneForOneStrategy({_, _ -> SupervisorDirective.Resume}, 1, null)
//        val childProps : Props = fromProducer{ -> ChildActor()}.withMailbox{ -> UnboundedMailbox.create(childMailboxStats)}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        childMailboxStats.reset.wait(1000)
//        Assert.contains(ResumeMailbox.Instance, childMailboxStats.posted)
//        Assert.contains(ResumeMailbox.Instance, childMailboxStats.received)
//    }
//    fun oneForOneStrategy_Should_StopChildOnFailure () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({_, _ -> SupervisorDirective.Stop}, 1, null)
//        val childProps : Props = fromProducer{ -> ChildActor()}.withMailbox{ -> UnboundedMailbox.create(childMailboxStats)}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        childMailboxStats.reset.wait(1000)
//        Assert.contains(Stop.Instance, childMailboxStats.posted)
//        Assert.contains(Stop.Instance, childMailboxStats.received)
//    }
//    fun oneForOneStrategy_Should_RestartChildOnFailure () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({_, _ -> SupervisorDirective.Restart}, 1, null)
//        val childProps : Props = fromProducer{ -> ChildActor()}.withMailbox{ -> UnboundedMailbox.create(childMailboxStats)}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        childMailboxStats.reset.wait(1000)
//        Assert.contains(childMailboxStats.posted, {it is Restart})
//        Assert.contains(childMailboxStats.received, {it is Restart})
//    }
//    fun oneForOneStrategy_Should_PassExceptionOnRestart () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({pid, reason -> SupervisorDirective.Restart}, 1, null)
//        val childProps : Props = fromProducer{ -> ChildActor()}.withMailbox{ -> UnboundedMailbox.create(childMailboxStats)}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        childMailboxStats.reset.wait(1000)
//        Assert.contains(childMailboxStats.posted, {msg -> (msg is Restart) && msg.reason == Exception})
//        Assert.contains(childMailboxStats.received, {msg -> (msg is Restart ) && msg.reason == Exception})
//    }
//    fun oneForOneStrategy_Should_StopChildWhenRestartLimitReached () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({pid, reason -> SupervisorDirective.Restart}, 1, null)
//        val childProps : Props = fromProducer{ -> ChildActor()}.withMailbox{ -> UnboundedMailbox.create(childMailboxStats)}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        parent.send("hello")
//        childMailboxStats.reset.wait(1000)
//        Assert.contains(StopInstance, childMailboxStats.posted)
//        Assert.contains(StopInstance, childMailboxStats.received)
//    }
//    fun oneForOneStrategy_WhenEscalateDirectiveWithoutGrandparent_ShouldRevertToDefaultDirective () {
//        val parentMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({_, _ -> SupervisorDirective.Escalate}, 1, null)
//        val childProps : Props = fromProducer{ -> ThrowOnStartedChildActor()}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy).withMailbox{ -> UnboundedMailbox.create(parentMailboxStats)}
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        parentMailboxStats.reset.wait(1000)
//        Assert.assertEquals(11, )
//        val failures :  = ..
//        for(failure in failures) {
//            if (failure.reason is AggregateException /* ae  */) {
//                Assert.isType<Exception>(ae.innerException)
//            } else {
//                Assert.isType<Exception>(failure.reason)
//            }
//        }
//    }
//    fun oneForOneStrategy_Should_EscalateFailureToParent () {
//        val parentMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({_, _ -> SupervisorDirective.Escalate}, 1, null)
//        val childProps : Props = fromProducer{ -> ChildActor()}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy).withMailbox{ -> UnboundedMailbox.create(parentMailboxStats)}
//        val parent : PID = spawn(parentProps)
//        parent.send("hello")
//        parentMailboxStats.reset.wait(1000)
//        val failure : Failure =
//        Assert.isType<Exception>(failure.reason)
//    }
//    fun oneForOneStrategy_Should_StopChildOnFailureWhenStarted () {
//        val childMailboxStats : TestMailboxStatistics = TestMailboxStatistics{it is Stopped}
//        val strategy : OneForOneStrategy = OneForOneStrategy({_, _ -> SupervisorDirective.Stop}, 1, null)
//        val childProps : Props = fromProducer{ -> ThrowOnStartedChildActor()}.withMailbox{ -> UnboundedMailbox.create(childMailboxStats)}
//        val parentProps : Props = fromProducer{ -> ParentActor(childProps)}.withChildSupervisorStrategy(strategy)
//        val parent : PID = spawn(parentProps)
//        childMailboxStats.reset.wait(1000)
//        Assert.contains(StopInstance, childMailboxStats.posted)
//        Assert.contains(StopInstance, childMailboxStats.received)
//    }
//}
//
